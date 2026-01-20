package com.oj.cs.service.impl;

import java.util.Date;
import java.util.List;

import jakarta.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oj.cs.common.ErrorCode;
import com.oj.cs.exception.BusinessException;
import com.oj.cs.mapper.TagMapper;
import com.oj.cs.model.dto.tag.TagQueryRequest;
import com.oj.cs.model.entity.Tag;
import com.oj.cs.model.entity.User;
import com.oj.cs.service.TagService;
import com.oj.cs.service.UserService;

import lombok.extern.slf4j.Slf4j;

/** 标签服务实现 */
@Service
@Slf4j
public class TagServiceImpl implements TagService {

  @Resource private TagMapper tagMapper;

  @Resource private UserService userService;

  @Override
  public List<Tag> getTagList() {
    LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.orderByAsc(Tag::getType).orderByAsc(Tag::getCreateTime);
    return tagMapper.selectList(queryWrapper);
  }

  @Override
  public Page<Tag> getTagListByPage(TagQueryRequest tagQueryRequest) {
    LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();

    // 根据标签名称模糊查询
    if (StringUtils.isNotBlank(tagQueryRequest.getName())) {
      queryWrapper.like(Tag::getName, tagQueryRequest.getName());
    }

    // 根据标签类型查询
    if (tagQueryRequest.getType() != null) {
      queryWrapper.eq(Tag::getType, tagQueryRequest.getType());
    }

    // 排序
    queryWrapper.orderByAsc(Tag::getType).orderByAsc(Tag::getCreateTime);

    // 分页查询
    long current = tagQueryRequest.getCurrent();
    long pageSize = tagQueryRequest.getPageSize();
    Page<Tag> tagPage = new Page<>(current, pageSize);

    return tagMapper.selectPage(tagPage, queryWrapper);
  }

  @Override
  public long createTag(Tag tag, Long loginUserId) {
    // 校验参数
    if (tag == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    if (tag.getName() == null || tag.getName().isEmpty()) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签名称不能为空");
    }

    // 检查标签名是否已存在
    LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(Tag::getName, tag.getName());
    long count = tagMapper.selectCount(queryWrapper);
    if (count > 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签名称已存在");
    }

    // 设置创建者和时间
    tag.setUserId(loginUserId);
    tag.setCreateTime(new Date());
    tag.setUpdateTime(new Date());

    // 插入数据
    int result = tagMapper.insert(tag);
    if (result <= 0) {
      throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建标签失败");
    }
    return tag.getId();
  }

  @Override
  public boolean updateTag(Tag tag, Long loginUserId) {
    // 校验参数
    if (tag == null || tag.getId() == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    // 校验权限
    Tag oldTag = tagMapper.selectById(tag.getId());
    if (oldTag == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
    }
    User loginUser = userService.getById(loginUserId);
    if (!loginUser.getUserRole().equals("admin") && !oldTag.getUserId().equals(loginUserId)) {
      throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
    }

    // 检查标签名是否已存在（排除自身）
    if (tag.getName() != null
        && !tag.getName().isEmpty()
        && !tag.getName().equals(oldTag.getName())) {
      LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
      queryWrapper.eq(Tag::getName, tag.getName());
      long count = tagMapper.selectCount(queryWrapper);
      if (count > 0) {
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签名称已存在");
      }
    }

    // 更新时间
    tag.setUpdateTime(new Date());

    // 更新数据
    int result = tagMapper.updateById(tag);
    return result > 0;
  }

  @Override
  public boolean deleteTag(long id, Long loginUserId) {
    // 校验参数
    if (id <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    // 校验权限
    Tag tag = tagMapper.selectById(id);
    if (tag == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
    }
    User loginUser = userService.getById(loginUserId);
    if (!loginUser.getUserRole().equals("admin") && !tag.getUserId().equals(loginUserId)) {
      throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
    }

    // 删除数据
    int result = tagMapper.deleteById(id);
    return result > 0;
  }
}
