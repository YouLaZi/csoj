package com.oj.cs.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.ResourceUtils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;

/** EasyExcel 测试 */
@SpringBootTest
public class EasyExcelTest {

  @Test
  public void doImport() throws FileNotFoundException {
    File file = ResourceUtils.getFile("classpath:test_excel.xlsx");
    List<Map<Integer, String>> list =
        EasyExcel.read(file).excelType(ExcelTypeEnum.XLSX).sheet().headRowNumber(0).doReadSync();
    System.out.println(list);
  }
}
