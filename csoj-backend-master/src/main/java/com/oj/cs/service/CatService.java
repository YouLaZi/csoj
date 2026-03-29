package com.oj.cs.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oj.cs.model.entity.CatItem;
import com.oj.cs.model.entity.UserCat;
import com.oj.cs.model.entity.UserCatInventory;

/** 猫咪养成系统服务 */
public interface CatService extends IService<UserCat> {

  /**
   * 获取或创建用户猫咪
   *
   * @param userId 用户ID
   * @return 用户猫咪
   */
  UserCat getOrCreateUserCat(Long userId);

  /**
   * 喂食猫咪
   *
   * @param userId 用户ID
   * @param itemCode 物品代码
   * @return 更新后的猫咪
   */
  UserCat feedCat(Long userId, String itemCode);

  /**
   * 与猫咪玩耍
   *
   * @param userId 用户ID
   * @param toyCode 玩具代码
   * @return 更新后的猫咪
   */
  UserCat playWithCat(Long userId, String toyCode);

  /**
   * 抚摸猫咪
   *
   * @param userId 用户ID
   * @return 更新后的猫咪
   */
  UserCat petCat(Long userId);

  /**
   * 让猫咪睡觉
   *
   * @param userId 用户ID
   * @return 更新后的猫咪
   */
  UserCat sleepCat(Long userId);

  /**
   * 更新猫咪状态(定时调用)
   *
   * @param userId 用户ID
   */
  void updateCatStats(Long userId);

  /**
   * 购买物品
   *
   * @param userId 用户ID
   * @param itemCode 物品代码
   * @param quantity 数量
   * @return 是否成功
   */
  boolean buyItem(Long userId, String itemCode, int quantity);

  /**
   * 获取用户物品背包
   *
   * @param userId 用户ID
   * @return 物品列表
   */
  List<UserCatInventory> getUserInventory(Long userId);

  /**
   * 获取商店物品
   *
   * @param itemType 物品类型(可选)
   * @return 物品列表
   */
  List<CatItem> getShopItems(String itemType);

  /**
   * 设置猫咪名字
   *
   * @param userId 用户ID
   * @param name 猫咪名字
   * @return 更新后的猫咪
   */
  UserCat setCatName(Long userId, String name);

  /**
   * 用户成功时猫咪反应
   *
   * @param userId 用户ID
   */
  void onUserSuccess(Long userId);

  /**
   * 增加猫咪经验值
   *
   * @param userId 用户ID
   * @param experience 经验值
   * @return 是否升级
   */
  boolean addExperience(Long userId, int experience);
}
