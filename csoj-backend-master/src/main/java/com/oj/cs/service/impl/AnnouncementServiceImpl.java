package com.oj.cs.service.impl;

import java.util.Date;
import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oj.cs.common.ErrorCode;
import com.oj.cs.exception.BusinessException;
import com.oj.cs.mapper.AnnouncementMapper;
import com.oj.cs.model.entity.Announcement;
import com.oj.cs.model.entity.User;
import com.oj.cs.service.AnnouncementService;
import com.oj.cs.service.UserService;

import lombok.extern.slf4j.Slf4j;

/** 系统公告服务实现 */
@Service
@Slf4j
public class AnnouncementServiceImpl implements AnnouncementService {

  @Resource private AnnouncementMapper announcementMapper;

  @Resource private UserService userService;

  @Override
  public Page<Announcement> getAnnouncementList(long page, long pageSize) {
    LambdaQueryWrapper<Announcement> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper
        .eq(Announcement::getStatus, 1) // 已发布的公告
        .orderByDesc(Announcement::getCreateTime);
    Page<Announcement> announcementPage = new Page<>(page, pageSize);
    return announcementMapper.selectPage(announcementPage, queryWrapper);
  }

  @Override
  public List<Announcement> getLatestAnnouncements(int count) {
    LambdaQueryWrapper<Announcement> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper
        .eq(Announcement::getStatus, 1) // 已发布的公告
        .orderByDesc(Announcement::getCreateTime)
        .last("LIMIT " + count);
    return announcementMapper.selectList(queryWrapper);
  }

  @Override
  public long createAnnouncement(Announcement announcement, Long loginUserId) {
    // 校验参数
    if (announcement == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    if (announcement.getTitle() == null || announcement.getTitle().isEmpty()) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "公告标题不能为空");
    }
    if (announcement.getContent() == null || announcement.getContent().isEmpty()) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "公告内容不能为空");
    }

    // 设置创建者和时间
    announcement.setUserId(loginUserId);
    announcement.setCreateTime(new Date());
    announcement.setUpdateTime(new Date());

    // 插入数据
    int result = announcementMapper.insert(announcement);
    if (result <= 0) {
      throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建公告失败");
    }
    return announcement.getId();
  }

  @Override
  public boolean updateAnnouncement(Announcement announcement, Long loginUserId) {
    // 校验参数
    if (announcement == null || announcement.getId() == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    // 校验权限
    Announcement oldAnnouncement = announcementMapper.selectById(announcement.getId());
    if (oldAnnouncement == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
    }
    User loginUser = userService.getById(loginUserId);
    if (!loginUser.getUserRole().equals("admin")
        && !oldAnnouncement.getUserId().equals(loginUserId)) {
      throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
    }

    // 更新时间
    announcement.setUpdateTime(new Date());

    // 更新数据
    int result = announcementMapper.updateById(announcement);
    return result > 0;
  }

  @Override
  public boolean deleteAnnouncement(long id, Long loginUserId) {
    // 校验参数
    if (id <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    // 校验权限
    Announcement announcement = announcementMapper.selectById(id);
    if (announcement == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
    }
    User loginUser = userService.getById(loginUserId);
    if (!loginUser.getUserRole().equals("admin") && !announcement.getUserId().equals(loginUserId)) {
      throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
    }

    // 删除数据
    int result = announcementMapper.deleteById(id);
    return result > 0;
  }
}
