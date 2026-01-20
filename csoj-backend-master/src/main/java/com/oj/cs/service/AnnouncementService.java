package com.oj.cs.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oj.cs.model.entity.Announcement;

/** 系统公告服务 */
public interface AnnouncementService {

  /**
   * 获取系统公告列表
   *
   * @param page 分页参数
   * @param pageSize 每页大小
   * @return 公告列表
   */
  Page<Announcement> getAnnouncementList(long page, long pageSize);

  /**
   * 获取最新公告
   *
   * @param count 获取数量
   * @return 公告列表
   */
  List<Announcement> getLatestAnnouncements(int count);

  /**
   * 创建公告
   *
   * @param announcement 公告信息
   * @param loginUserId 当前登录用户ID
   * @return 创建的公告ID
   */
  long createAnnouncement(Announcement announcement, Long loginUserId);

  /**
   * 更新公告
   *
   * @param announcement 公告信息
   * @param loginUserId 当前登录用户ID
   * @return 是否成功
   */
  boolean updateAnnouncement(Announcement announcement, Long loginUserId);

  /**
   * 删除公告
   *
   * @param id 公告ID
   * @param loginUserId 当前登录用户ID
   * @return 是否成功
   */
  boolean deleteAnnouncement(long id, Long loginUserId);
}
