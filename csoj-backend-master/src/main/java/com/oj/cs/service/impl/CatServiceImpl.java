package com.oj.cs.service.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oj.cs.common.ErrorCode;
import com.oj.cs.exception.BusinessException;
import com.oj.cs.mapper.CatItemMapper;
import com.oj.cs.mapper.UserCatInventoryMapper;
import com.oj.cs.mapper.UserCatMapper;
import com.oj.cs.mapper.UserPointsMapper;
import com.oj.cs.model.entity.CatItem;
import com.oj.cs.model.entity.UserCat;
import com.oj.cs.model.entity.UserCatInventory;
import com.oj.cs.model.entity.UserPoints;
import com.oj.cs.service.CatService;
import com.oj.cs.service.PointsService;

import lombok.extern.slf4j.Slf4j;

/** 猫咪养成系统服务实现 */
@Service
@Slf4j
public class CatServiceImpl extends ServiceImpl<UserCatMapper, UserCat> implements CatService {

  @Resource private UserCatMapper userCatMapper;

  @Resource private CatItemMapper catItemMapper;

  @Resource private UserCatInventoryMapper inventoryMapper;

  @Resource private UserPointsMapper userPointsMapper;

  @Resource private PointsService pointsService;

  // 升级所需经验值基数
  private static final int EXP_BASE = 100;
  // 状态衰减间隔(小时)
  private static final int DECAY_INTERVAL_HOURS = 4;
  // 每次衰减值
  private static final int DECAY_AMOUNT = 5;

  @Override
  public UserCat getOrCreateUserCat(Long userId) {
    LambdaQueryWrapper<UserCat> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(UserCat::getUserId, userId);
    UserCat userCat = userCatMapper.selectOne(queryWrapper);

    if (userCat == null) {
      // 创建新猫咪
      userCat = new UserCat();
      userCat.setUserId(userId);
      userCat.setCatName("小橘");
      userCat.setLevel(1);
      userCat.setExperience(0);
      userCat.setHunger(80);
      userCat.setHappiness(80);
      userCat.setHealth(100);
      userCat.setEnergy(100);
      userCat.setMood("happy");
      userCat.setTotalFeedCount(0);
      userCat.setTotalPlayCount(0);
      userCat.setCreateDay(
          Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
      userCatMapper.insert(userCat);
      log.info("为用户 {} 创建了新猫咪", userId);
    }

    return userCat;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public UserCat feedCat(Long userId, String itemCode) {
    UserCat userCat = getOrCreateUserCat(userId);

    // 检查物品
    CatItem item = getItemByCode(itemCode);
    if (item == null || !"food".equals(item.getItemType())) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的食物物品");
    }

    // 检查背包
    UserCatInventory inventory = getInventoryItem(userId, itemCode);
    if (inventory == null || inventory.getQuantity() <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "物品数量不足");
    }

    // 应用效果
    applyEffect(userCat, item.getEffect());

    // 更新猫咪状态
    userCat.setLastFeedTime(new Date());
    userCat.setTotalFeedCount(userCat.getTotalFeedCount() + 1);
    userCat.setMood("happy");

    // 减少物品数量
    inventory.setQuantity(inventory.getQuantity() - 1);
    inventoryMapper.updateById(inventory);

    // 增加经验
    addExperience(userCat, 5);

    userCatMapper.updateById(userCat);
    log.info("用户 {} 喂食猫咪，物品: {}", userId, itemCode);

    return userCat;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public UserCat playWithCat(Long userId, String toyCode) {
    UserCat userCat = getOrCreateUserCat(userId);

    // 检查精力
    if (userCat.getEnergy() < 20) {
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "猫咪太累了，需要休息");
    }

    // 检查物品
    CatItem item = getItemByCode(toyCode);
    if (item == null || !"toy".equals(item.getItemType())) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的玩具物品");
    }

    // 检查背包
    UserCatInventory inventory = getInventoryItem(userId, toyCode);
    if (inventory == null || inventory.getQuantity() <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "物品数量不足");
    }

    // 应用效果
    applyEffect(userCat, item.getEffect());

    // 更新猫咪状态
    userCat.setLastPlayTime(new Date());
    userCat.setTotalPlayCount(userCat.getTotalPlayCount() + 1);
    userCat.setMood("excited");

    // 减少物品数量
    inventory.setQuantity(inventory.getQuantity() - 1);
    inventoryMapper.updateById(inventory);

    // 增加经验
    addExperience(userCat, 8);

    userCatMapper.updateById(userCat);
    log.info("用户 {} 与猫咪玩耍，物品: {}", userId, toyCode);

    return userCat;
  }

  @Override
  public UserCat petCat(Long userId) {
    UserCat userCat = getOrCreateUserCat(userId);

    // 增加快乐值
    int newHappiness = Math.min(100, userCat.getHappiness() + 5);
    userCat.setHappiness(newHappiness);
    userCat.setMood("happy");

    // 增加少量经验
    addExperience(userCat, 2);

    userCatMapper.updateById(userCat);
    return userCat;
  }

  @Override
  public UserCat sleepCat(Long userId) {
    UserCat userCat = getOrCreateUserCat(userId);

    // 恢复精力
    userCat.setEnergy(100);
    userCat.setHealth(Math.min(100, userCat.getHealth() + 10));
    userCat.setMood("sleeping");
    userCat.setLastSleepTime(new Date());

    userCatMapper.updateById(userCat);
    log.info("用户 {} 让猫咪睡觉", userId);

    return userCat;
  }

  @Override
  public void updateCatStats(Long userId) {
    UserCat userCat = getOrCreateUserCat(userId);

    // 计算时间差
    Date now = new Date();
    long hoursSinceLastUpdate = calculateHoursSince(userCat.getUpdateTime());

    if (hoursSinceLastUpdate >= DECAY_INTERVAL_HOURS) {
      int decayCount = (int) (hoursSinceLastUpdate / DECAY_INTERVAL_HOURS);

      // 衰减饥饿值和快乐值
      int hungerDecay = decayCount * DECAY_AMOUNT;
      int happinessDecay = decayCount * (DECAY_AMOUNT / 2);

      userCat.setHunger(Math.max(0, userCat.getHunger() - hungerDecay));
      userCat.setHappiness(Math.max(0, userCat.getHappiness() - happinessDecay));

      // 更新心情
      updateMood(userCat);

      userCatMapper.updateById(userCat);
      log.info("更新用户 {} 猫咪状态，饥饿-{}/快乐-{}", userId, hungerDecay, happinessDecay);
    }
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public boolean buyItem(Long userId, String itemCode, int quantity) {
    if (quantity <= 0) {
      quantity = 1;
    }

    // 检查物品
    CatItem item = getItemByCode(itemCode);
    if (item == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "物品不存在");
    }

    int totalCost = item.getPrice() * quantity;

    // 检查积分
    UserPoints userPoints = getUserPoints(userId);
    if (userPoints.getTotalPoints() < totalCost) {
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "积分不足");
    }

    // 扣除积分
    userPoints.setTotalPoints(userPoints.getTotalPoints() - totalCost);
    userPointsMapper.updateById(userPoints);

    // 增加物品到背包
    UserCatInventory inventory = getInventoryItem(userId, itemCode);
    if (inventory == null) {
      inventory = new UserCatInventory();
      inventory.setUserId(userId);
      inventory.setItemCode(itemCode);
      inventory.setQuantity(quantity);
      inventoryMapper.insert(inventory);
    } else {
      inventory.setQuantity(inventory.getQuantity() + quantity);
      inventoryMapper.updateById(inventory);
    }

    log.info("用户 {} 购买物品 {} x{}", userId, itemCode, quantity);
    return true;
  }

  @Override
  public List<UserCatInventory> getUserInventory(Long userId) {
    LambdaQueryWrapper<UserCatInventory> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(UserCatInventory::getUserId, userId);
    queryWrapper.gt(UserCatInventory::getQuantity, 0);
    return inventoryMapper.selectList(queryWrapper);
  }

  @Override
  public List<CatItem> getShopItems(String itemType) {
    LambdaQueryWrapper<CatItem> queryWrapper = new LambdaQueryWrapper<>();
    if (itemType != null && !itemType.isEmpty()) {
      queryWrapper.eq(CatItem::getItemType, itemType);
    }
    queryWrapper.orderByAsc(CatItem::getPrice);
    return catItemMapper.selectList(queryWrapper);
  }

  @Override
  public UserCat setCatName(Long userId, String name) {
    if (name == null || name.trim().isEmpty() || name.length() > 16) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "猫咪名字长度应在1-16个字符之间");
    }

    UserCat userCat = getOrCreateUserCat(userId);
    userCat.setCatName(name.trim());
    userCatMapper.updateById(userCat);

    return userCat;
  }

  @Override
  public void onUserSuccess(Long userId) {
    UserCat userCat = getOrCreateUserCat(userId);
    userCat.setHappiness(Math.min(100, userCat.getHappiness() + 10));
    userCat.setMood("excited");
    addExperience(userCat, 10);
    userCatMapper.updateById(userCat);
  }

  @Override
  public boolean addExperience(Long userId, int experience) {
    UserCat userCat = getOrCreateUserCat(userId);
    return addExperience(userCat, experience);
  }

  // ========== 私有方法 ==========

  private boolean addExperience(UserCat userCat, int experience) {
    int newExp = userCat.getExperience() + experience;
    int level = userCat.getLevel();
    int expNeeded = getExpNeededForLevel(level);

    boolean leveledUp = false;
    while (newExp >= expNeeded && level < 100) {
      newExp -= expNeeded;
      level++;
      expNeeded = getExpNeededForLevel(level);
      leveledUp = true;
      log.info("猫咪升级到 {} 级!", level);
    }

    userCat.setExperience(newExp);
    userCat.setLevel(level);

    return leveledUp;
  }

  private int getExpNeededForLevel(int level) {
    return EXP_BASE * level;
  }

  private CatItem getItemByCode(String itemCode) {
    LambdaQueryWrapper<CatItem> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(CatItem::getItemCode, itemCode);
    return catItemMapper.selectOne(queryWrapper);
  }

  private UserCatInventory getInventoryItem(Long userId, String itemCode) {
    LambdaQueryWrapper<UserCatInventory> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(UserCatInventory::getUserId, userId);
    queryWrapper.eq(UserCatInventory::getItemCode, itemCode);
    return inventoryMapper.selectOne(queryWrapper);
  }

  private UserPoints getUserPoints(Long userId) {
    LambdaQueryWrapper<UserPoints> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(UserPoints::getUserId, userId);
    UserPoints userPoints = userPointsMapper.selectOne(queryWrapper);
    if (userPoints == null) {
      userPoints = new UserPoints();
      userPoints.setUserId(userId);
      userPoints.setTotalPoints(0);
      userPointsMapper.insert(userPoints);
    }
    return userPoints;
  }

  private void applyEffect(UserCat userCat, String effectJson) {
    if (effectJson == null || effectJson.isEmpty()) {
      return;
    }

    try {
      // 简单解析JSON效果
      effectJson = effectJson.replace("{", "").replace("}", "").replace("\"", "");
      String[] pairs = effectJson.split(",");
      for (String pair : pairs) {
        String[] kv = pair.split(":");
        if (kv.length == 2) {
          String key = kv[0].trim();
          int value = Integer.parseInt(kv[1].trim());

          switch (key) {
            case "hunger":
              userCat.setHunger(Math.min(100, Math.max(0, userCat.getHunger() + value)));
              break;
            case "happiness":
              userCat.setHappiness(Math.min(100, Math.max(0, userCat.getHappiness() + value)));
              break;
            case "health":
              userCat.setHealth(Math.min(100, Math.max(0, userCat.getHealth() + value)));
              break;
            case "energy":
              userCat.setEnergy(Math.min(100, Math.max(0, userCat.getEnergy() + value)));
              break;
          }
        }
      }
    } catch (Exception e) {
      log.warn("解析物品效果失败: {}", effectJson, e);
    }
  }

  private void updateMood(UserCat userCat) {
    int hunger = userCat.getHunger();
    int happiness = userCat.getHappiness();

    if (hunger < 20 || happiness < 20) {
      userCat.setMood("sad");
    } else if (hunger < 40 || happiness < 40) {
      userCat.setMood("thinking");
    } else {
      userCat.setMood("happy");
    }
  }

  private long calculateHoursSince(Date date) {
    if (date == null) {
      return 0;
    }
    long diffMs = System.currentTimeMillis() - date.getTime();
    return diffMs / (1000 * 60 * 60);
  }
}
