package com.oj.cs.model.dto.mobile;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/** 移动端分页响应 优化数据传输，减少不必要字段 */
@Data
public class MobilePageResponse<T> implements Serializable {

  /** 数据列表 */
  private List<T> records;

  /** 当前页码 */
  private Integer current;

  /** 每页数量 */
  private Integer size;

  /** 总记录数 */
  private Long total;

  /** 总页数 */
  private Integer pages;

  /** 是否有下一页 */
  private Boolean hasNext;

  /** 是否有上一页 */
  private Boolean hasPrevious;

  /** 移动端是否还有更多数据（简化判断） */
  private Boolean hasMore;

  /** 下一页游标（用于游标分页） */
  private String nextCursor;

  /** 数据摘要（用于快速展示） */
  private MobileSummary summary;

  /** 数据摘要信息 */
  @Data
  public static class MobileSummary {
    /** 当前页记录数 */
    private Integer currentCount;

    /** 总计（数值型统计） */
    private Long totalCount;

    /** 提示信息 */
    private String message;
  }

  /** 构建移动端分页响应 */
  public static <T> MobilePageResponse<T> build(
      List<T> records, Integer current, Integer size, Long total) {
    MobilePageResponse<T> response = new MobilePageResponse<>();
    response.setRecords(records);
    response.setCurrent(current);
    response.setSize(size);
    response.setTotal(total);

    // 计算总页数
    int pages = (int) Math.ceil((double) total / size);
    response.setPages(pages);

    // 是否有下一页
    response.setHasNext(current < pages);
    response.setHasPrevious(current > 1);

    // 简化判断：是否还有更多
    response.setHasMore(current < pages);

    // 构建摘要
    MobileSummary summary = new MobileSummary();
    summary.setCurrentCount(records.size());
    summary.setTotalCount(total);
    summary.setMessage(String.format("共 %d 条记录", total));
    response.setSummary(summary);

    return response;
  }

  /** 构建空响应 */
  public static <T> MobilePageResponse<T> empty() {
    return build(new java.util.ArrayList<>(), 1, 10, 0L);
  }

  /** 构建游标分页响应（适用于无限滚动） */
  public static <T> MobilePageResponse<T> buildWithCursor(
      List<T> records, String nextCursor, Boolean hasMore) {
    MobilePageResponse<T> response = new MobilePageResponse<>();
    response.setRecords(records);
    response.setNextCursor(nextCursor);
    response.setHasMore(hasMore);

    MobileSummary summary = new MobileSummary();
    summary.setCurrentCount(records.size());
    summary.setMessage(hasMore ? "更多数据可加载" : "已加载全部数据");
    response.setSummary(summary);

    return response;
  }

  private static final long serialVersionUID = 1L;
}
