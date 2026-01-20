package com.oj.cs.controller;

import java.util.List;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oj.cs.annotation.AuthCheck;
import com.oj.cs.common.BaseResponse;
import com.oj.cs.common.ErrorCode;
import com.oj.cs.common.ResultUtils;
import com.oj.cs.exception.BusinessException;
import com.oj.cs.model.dto.tag.TagQueryRequest;
import com.oj.cs.model.entity.Tag;
import com.oj.cs.model.entity.User;
import com.oj.cs.service.TagService;
import com.oj.cs.service.UserService;

import lombok.extern.slf4j.Slf4j;

/** 标签接口 */
@RestController
@RequestMapping("/tag")
@Slf4j
public class TagController {

  @Resource private TagService tagService;

  @Resource private UserService userService;

  /**
   * 获取标签列表
   *
   * @return 标签列表
   */
  @GetMapping("/list")
  public BaseResponse<List<Tag>> getTagList() {
    List<Tag> tagList = tagService.getTagList();
    return ResultUtils.success(tagList);
  }

  /**
   * 分页获取标签列表
   *
   * @param tagQueryRequest 查询请求
   * @return 标签分页列表
   */
  @PostMapping("/list/page")
  public BaseResponse<Page<Tag>> getTagListByPage(@RequestBody TagQueryRequest tagQueryRequest) {
    if (tagQueryRequest == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    Page<Tag> tagPage = tagService.getTagListByPage(tagQueryRequest);
    return ResultUtils.success(tagPage);
  }

  /**
   * 添加标签
   *
   * @param tag 标签信息
   * @param request HTTP请求
   * @return 创建的标签ID
   */
  @PostMapping("/add")
  @AuthCheck(mustRole = "admin")
  public BaseResponse<Long> addTag(@RequestBody Tag tag, HttpServletRequest request) {
    if (tag == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    User loginUser = userService.getLoginUser(request);
    long tagId = tagService.createTag(tag, loginUser.getId());
    return ResultUtils.success(tagId);
  }

  /**
   * 更新标签
   *
   * @param tag 标签信息
   * @param request HTTP请求
   * @return 是否成功
   */
  @PostMapping("/update")
  @AuthCheck(mustRole = "admin")
  public BaseResponse<Boolean> updateTag(@RequestBody Tag tag, HttpServletRequest request) {
    if (tag == null || tag.getId() == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    User loginUser = userService.getLoginUser(request);
    boolean result = tagService.updateTag(tag, loginUser.getId());
    return ResultUtils.success(result);
  }

  /**
   * 删除标签
   *
   * @param id 标签ID
   * @param request HTTP请求
   * @return 是否成功
   */
  @PostMapping("/delete")
  @AuthCheck(mustRole = "admin")
  public BaseResponse<Boolean> deleteTag(@RequestParam long id, HttpServletRequest request) {
    if (id <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    User loginUser = userService.getLoginUser(request);
    boolean result = tagService.deleteTag(id, loginUser.getId());
    return ResultUtils.success(result);
  }
}
