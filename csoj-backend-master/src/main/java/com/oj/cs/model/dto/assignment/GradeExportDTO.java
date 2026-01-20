package com.oj.cs.model.dto.assignment;

import java.io.Serializable;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;

import lombok.Data;

/** 成绩导出 DTO */
@Data
@HeadRowHeight(20)
@ContentRowHeight(18)
public class GradeExportDTO implements Serializable {

  private static final long serialVersionUID = 1L;

  @ExcelProperty("序号")
  @ColumnWidth(8)
  private Integer index;

  @ExcelProperty("学生学号")
  @ColumnWidth(15)
  private String studentNo;

  @ExcelProperty("学生姓名")
  @ColumnWidth(15)
  private String studentName;

  @ExcelProperty("作业标题")
  @ColumnWidth(25)
  private String assignmentTitle;

  @ExcelProperty("总分")
  @ColumnWidth(10)
  private Integer totalScore;

  @ExcelProperty("及格分")
  @ColumnWidth(10)
  private Integer passScore;

  @ExcelProperty("得分")
  @ColumnWidth(10)
  private Integer score;

  @ExcelProperty("是否及格")
  @ColumnWidth(12)
  private String isPassed;

  @ExcelProperty("提交时间")
  @ColumnWidth(20)
  private String submitTime;

  @ExcelProperty("批改时间")
  @ColumnWidth(20)
  private String gradedTime;

  @ExcelProperty("批改教师")
  @ColumnWidth(15)
  private String teacherName;

  @ExcelProperty("评语")
  @ColumnWidth(30)
  private String comment;
}
