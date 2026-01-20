package com.oj.cs.listener;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Resource;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.oj.cs.model.dto.user.StudentImportDTO;
import com.oj.cs.model.entity.User;
import com.oj.cs.model.enums.UserRoleEnum;
import com.oj.cs.service.UserService;
import com.oj.cs.utils.PasswordUtil;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/** 学生导入监听器 用于处理 Excel 数据导入 */
@Slf4j
public class StudentImportListener extends AnalysisEventListener<StudentImportDTO> {

  @Resource private UserService userService;

  /** 每隔 100 条数据存储到数据库，然后清理 list，方便内存回收 */
  private static final int BATCH_SIZE = 100;

  /** 导入的数据列表 */
  private List<StudentImportDTO> dataList = new ArrayList<>();

  /** 成功导入数量 */
  private int successCount = 0;

  /** 失败数量 */
  private int failCount = 0;

  /** 错误信息列表 */
  private List<String> errorMessages = new ArrayList<>();

  /** 如果使用了 spring，请使用这个构造方法 每次 createListener 都需要把 spring 管理的类传进来 */
  public StudentImportListener(UserService userService) {
    this.userService = userService;
  }

  /** 这个每一条数据解析都会来调用 */
  @Override
  public void invoke(StudentImportDTO data, AnalysisContext context) {
    dataList.add(data);

    // 达到 BATCH_SIZE 了，需要去存储一次数据库，防止数据几万条数据在内存，容易 OOM
    if (dataList.size() >= BATCH_SIZE) {
      saveData();
      // 存储完成清理 list
      dataList = new ArrayList<>(BATCH_SIZE);
    }
  }

  /** 所有数据解析完成 都会来调用 */
  @Override
  public void doAfterAllAnalysed(AnalysisContext context) {
    // 这里也要保存数据，确保最后遗留的数据也存储到数据库
    saveData();
    log.info("学生导入完成，成功: {}，失败: {}", successCount, failCount);
  }

  /** 加上存储数据库 */
  private void saveData() {
    if (dataList.isEmpty()) {
      return;
    }

    log.info("开始处理 {} 条学生数据", dataList.size());

    for (int i = 0; i < dataList.size(); i++) {
      StudentImportDTO dto = dataList.get(i);
      try {
        // 数据校验
        if (StrUtil.isBlank(dto.getStudentNo())) {
          failCount++;
          errorMessages.add("第 " + (contextReadRowNum(i)) + " 行：学号不能为空");
          continue;
        }

        if (StrUtil.isBlank(dto.getUserNameReal())) {
          failCount++;
          errorMessages.add("第 " + (contextReadRowNum(i)) + " 行：姓名不能为空");
          continue;
        }

        // 设置默认值
        if (StrUtil.isBlank(dto.getUserName())) {
          dto.setUserName(dto.getStudentNo());
        }

        if (StrUtil.isBlank(dto.getUserPassword())) {
          dto.setUserPassword("12345678"); // 默认密码（至少8位）
        }

        // 验证密码长度
        if (dto.getUserPassword().length() < 8) {
          failCount++;
          errorMessages.add("第 " + (contextReadRowNum(i)) + " 行：密码长度不能少于8位");
          continue;
        }

        // 检查学号是否已存在
        User existingUser =
            userService.lambdaQuery().eq(User::getUserAccount, dto.getStudentNo()).one();

        if (existingUser != null) {
          // 更新现有用户信息
          existingUser.setUserName(dto.getUserNameReal());
          if (StrUtil.isNotBlank(dto.getUserEmail())) {
            existingUser.setEmail(dto.getUserEmail());
          }
          userService.updateById(existingUser);
          log.info("更新学生信息: {}", dto.getStudentNo());
        } else {
          // 创建新用户
          User user = new User();
          user.setUserAccount(dto.getStudentNo());
          user.setUserName(dto.getUserNameReal());
          user.setUserRole(UserRoleEnum.USER.getValue());
          // 使用 BCrypt 加密密码
          String encryptedPassword = PasswordUtil.hash(dto.getUserPassword());
          user.setUserPassword(encryptedPassword);
          if (StrUtil.isNotBlank(dto.getUserEmail())) {
            user.setEmail(dto.getUserEmail());
          }
          userService.save(user);
          log.info("创建新学生: {}", dto.getStudentNo());
        }

        successCount++;
      } catch (Exception e) {
        failCount++;
        errorMessages.add("第 " + (contextReadRowNum(i)) + " 行：" + e.getMessage());
        log.error("导入学生数据失败: {}", dto.getStudentNo(), e);
      }
    }
  }

  /** 获取当前读取的行号（用于错误提示） */
  private String contextReadRowNum(int index) {
    return String.valueOf(index + 1);
  }

  /** 获取导入结果 */
  public ImportResult getResult() {
    return new ImportResult(successCount, failCount, errorMessages);
  }

  /** 导入结果 */
  public static class ImportResult {
    private final int successCount;
    private final int failCount;
    private final List<String> errorMessages;

    public ImportResult(int successCount, int failCount, List<String> errorMessages) {
      this.successCount = successCount;
      this.failCount = failCount;
      this.errorMessages = errorMessages;
    }

    public int getSuccessCount() {
      return successCount;
    }

    public int getFailCount() {
      return failCount;
    }

    public List<String> getErrorMessages() {
      return errorMessages;
    }

    public int getTotalCount() {
      return successCount + failCount;
    }
  }
}
