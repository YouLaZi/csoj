package com.oj.cs.controller;

import java.util.List;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.*;

import com.oj.cs.common.BaseResponse;
import com.oj.cs.common.ResultUtils;
import com.oj.cs.model.entity.CatItem;
import com.oj.cs.model.entity.UserCat;
import com.oj.cs.model.entity.UserCatInventory;
import com.oj.cs.service.CatService;
import com.oj.cs.service.UserService;

import lombok.extern.slf4j.Slf4j;

/** 猫咪养成接口 */
@RestController
@RequestMapping("/cat")
@Slf4j
public class CatController {

  @Resource private CatService catService;

  @Resource private UserService userService;

  /** 获取用户猫咪 */
  @GetMapping
  public BaseResponse<UserCat> getCat(HttpServletRequest request) {
    Long userId = userService.getLoginUser(request).getId();
    UserCat cat = catService.getOrCreateUserCat(userId);
    return ResultUtils.success(cat);
  }

  /** 喂食猫咪 */
  @PostMapping("/feed")
  public BaseResponse<UserCat> feedCat(@RequestParam String itemCode, HttpServletRequest request) {
    Long userId = userService.getLoginUser(request).getId();
    UserCat cat = catService.feedCat(userId, itemCode);
    return ResultUtils.success(cat);
  }

  /** 与猫咪玩耍 */
  @PostMapping("/play")
  public BaseResponse<UserCat> playWithCat(
      @RequestParam String toyCode, HttpServletRequest request) {
    Long userId = userService.getLoginUser(request).getId();
    UserCat cat = catService.playWithCat(userId, toyCode);
    return ResultUtils.success(cat);
  }

  /** 抚摸猫咪 */
  @PostMapping("/pet")
  public BaseResponse<UserCat> petCat(HttpServletRequest request) {
    Long userId = userService.getLoginUser(request).getId();
    UserCat cat = catService.petCat(userId);
    return ResultUtils.success(cat);
  }

  /** 让猫咪睡觉 */
  @PostMapping("/sleep")
  public BaseResponse<UserCat> sleepCat(HttpServletRequest request) {
    Long userId = userService.getLoginUser(request).getId();
    UserCat cat = catService.sleepCat(userId);
    return ResultUtils.success(cat);
  }

  /** 设置猫咪名字 */
  @PutMapping("/name")
  public BaseResponse<UserCat> setCatName(@RequestParam String name, HttpServletRequest request) {
    Long userId = userService.getLoginUser(request).getId();
    UserCat cat = catService.setCatName(userId, name);
    return ResultUtils.success(cat);
  }

  /** 获取商店物品 */
  @GetMapping("/shop")
  public BaseResponse<List<CatItem>> getShopItems(@RequestParam(required = false) String itemType) {
    List<CatItem> items = catService.getShopItems(itemType);
    return ResultUtils.success(items);
  }

  /** 购买物品 */
  @PostMapping("/buy")
  public BaseResponse<Boolean> buyItem(
      @RequestParam String itemCode,
      @RequestParam(defaultValue = "1") int quantity,
      HttpServletRequest request) {
    Long userId = userService.getLoginUser(request).getId();
    boolean success = catService.buyItem(userId, itemCode, quantity);
    return ResultUtils.success(success);
  }

  /** 获取用户物品背包 */
  @GetMapping("/inventory")
  public BaseResponse<List<UserCatInventory>> getInventory(HttpServletRequest request) {
    Long userId = userService.getLoginUser(request).getId();
    List<UserCatInventory> inventory = catService.getUserInventory(userId);
    return ResultUtils.success(inventory);
  }
}
