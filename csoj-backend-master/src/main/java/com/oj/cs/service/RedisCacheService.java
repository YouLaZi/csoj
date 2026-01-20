package com.oj.cs.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/** Redis缓存服务 提供常用的缓存操作方法 */
@Slf4j
@Service
public class RedisCacheService {

  @Autowired private RedisTemplate<String, Object> redisTemplate;

  // =============================  基本操作  =============================

  /**
   * 设置缓存
   *
   * @param key 键
   * @param value 值
   */
  public void set(String key, Object value) {
    try {
      redisTemplate.opsForValue().set(key, value);
    } catch (Exception e) {
      log.error("设置缓存失败, key: {}", key, e);
    }
  }

  /**
   * 设置缓存并设置过期时间
   *
   * @param key 键
   * @param value 值
   * @param timeout 过期时间（秒）
   */
  public void set(String key, Object value, long timeout) {
    try {
      redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    } catch (Exception e) {
      log.error("设置缓存失败, key: {}, timeout: {}", key, timeout, e);
    }
  }

  /**
   * 设置缓存并设置过期时间
   *
   * @param key 键
   * @param value 值
   * @param timeout 过期时间
   * @param unit 时间单位
   */
  public void set(String key, Object value, long timeout, TimeUnit unit) {
    try {
      redisTemplate.opsForValue().set(key, value, timeout, unit);
    } catch (Exception e) {
      log.error("设置缓存失败, key: {}, timeout: {}, unit: {}", key, timeout, unit, e);
    }
  }

  /**
   * 获取缓存
   *
   * @param key 键
   * @return 值
   */
  public Object get(String key) {
    try {
      return redisTemplate.opsForValue().get(key);
    } catch (Exception e) {
      log.error("获取缓存失败, key: {}", key, e);
      return null;
    }
  }

  /**
   * 获取缓存并指定类型
   *
   * @param key 键
   * @param clazz 类型
   * @return 值
   */
  @SuppressWarnings("unchecked")
  public <T> T get(String key, Class<T> clazz) {
    try {
      Object value = redisTemplate.opsForValue().get(key);
      return value != null ? (T) value : null;
    } catch (Exception e) {
      log.error("获取缓存失败, key: {}, class: {}", key, clazz.getName(), e);
      return null;
    }
  }

  /**
   * 删除缓存
   *
   * @param key 键
   */
  public void delete(String key) {
    try {
      redisTemplate.delete(key);
    } catch (Exception e) {
      log.error("删除缓存失败, key: {}", key, e);
    }
  }

  /**
   * 批量删除缓存
   *
   * @param keys 键集合
   */
  public void delete(Collection<String> keys) {
    try {
      redisTemplate.delete(keys);
    } catch (Exception e) {
      log.error("批量删除缓存失败, keys: {}", keys, e);
    }
  }

  /**
   * 判断key是否存在
   *
   * @param key 键
   * @return 是否存在
   */
  public boolean hasKey(String key) {
    try {
      return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    } catch (Exception e) {
      log.error("判断key是否存在失败, key: {}", key, e);
      return false;
    }
  }

  /**
   * 设置过期时间
   *
   * @param key 键
   * @param timeout 过期时间（秒）
   * @return 是否设置成功
   */
  public boolean expire(String key, long timeout) {
    try {
      return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, TimeUnit.SECONDS));
    } catch (Exception e) {
      log.error("设置过期时间失败, key: {}, timeout: {}", key, timeout, e);
      return false;
    }
  }

  /**
   * 获取过期时间
   *
   * @param key 键
   * @return 过期时间（秒）
   */
  public long getExpire(String key) {
    try {
      Long expire = redisTemplate.getExpire(key);
      return expire != null ? expire : -1;
    } catch (Exception e) {
      log.error("获取过期时间失败, key: {}", key, e);
      return -1;
    }
  }

  // =============================  Hash操作  =============================

  /**
   * 设置Hash缓存
   *
   * @param key 键
   * @param hashKey Hash键
   * @param value 值
   */
  public void hSet(String key, String hashKey, Object value) {
    try {
      redisTemplate.opsForHash().put(key, hashKey, value);
    } catch (Exception e) {
      log.error("设置Hash缓存失败, key: {}, hashKey: {}", key, hashKey, e);
    }
  }

  /**
   * 获取Hash缓存
   *
   * @param key 键
   * @param hashKey Hash键
   * @return 值
   */
  public Object hGet(String key, String hashKey) {
    try {
      return redisTemplate.opsForHash().get(key, hashKey);
    } catch (Exception e) {
      log.error("获取Hash缓存失败, key: {}, hashKey: {}", key, hashKey, e);
      return null;
    }
  }

  /**
   * 获取所有Hash值
   *
   * @param key 键
   * @return Hash映射
   */
  public Map<Object, Object> hGetAll(String key) {
    try {
      return redisTemplate.opsForHash().entries(key);
    } catch (Exception e) {
      log.error("获取所有Hash值失败, key: {}", key, e);
      return null;
    }
  }

  /**
   * 删除Hash值
   *
   * @param key 键
   * @param hashKeys Hash键集合
   */
  public void hDelete(String key, Object... hashKeys) {
    try {
      redisTemplate.opsForHash().delete(key, hashKeys);
    } catch (Exception e) {
      log.error("删除Hash值失败, key: {}, hashKeys: {}", key, hashKeys, e);
    }
  }

  // =============================  Set操作  =============================

  /**
   * 设置Set缓存
   *
   * @param key 键
   * @param values 值集合
   */
  public void sAdd(String key, Object... values) {
    try {
      redisTemplate.opsForSet().add(key, values);
    } catch (Exception e) {
      log.error("设置Set缓存失败, key: {}", key, e);
    }
  }

  /**
   * 获取Set缓存
   *
   * @param key 键
   * @return 值集合
   */
  public Set<Object> sMembers(String key) {
    try {
      return redisTemplate.opsForSet().members(key);
    } catch (Exception e) {
      log.error("获取Set缓存失败, key: {}", key, e);
      return null;
    }
  }

  /**
   * 判断Set中是否存在值
   *
   * @param key 键
   * @param value 值
   * @return 是否存在
   */
  public boolean sIsMember(String key, Object value) {
    try {
      return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
    } catch (Exception e) {
      log.error("判断Set中是否存在值失败, key: {}, value: {}", key, value, e);
      return false;
    }
  }

  // =============================  ZSet操作  =============================

  /**
   * 设置ZSet缓存
   *
   * @param key 键
   * @param value 值
   * @param score 分数
   */
  public void zAdd(String key, Object value, double score) {
    try {
      redisTemplate.opsForZSet().add(key, value, score);
    } catch (Exception e) {
      log.error("设置ZSet缓存失败, key: {}, value: {}, score: {}", key, value, score, e);
    }
  }

  /**
   * 获取ZSet排名（从高到低）
   *
   * @param key 键
   * @param start 起始位置
   * @param end 结束位置
   * @return 值集合
   */
  public Set<Object> zReverseRange(String key, long start, long end) {
    try {
      return redisTemplate.opsForZSet().reverseRange(key, start, end);
    } catch (Exception e) {
      log.error("获取ZSet排名失败, key: {}, start: {}, end: {}", key, start, end, e);
      return null;
    }
  }

  /**
   * 获取ZSet中值的分数
   *
   * @param key 键
   * @param value 值
   * @return 分数
   */
  public Double zScore(String key, Object value) {
    try {
      return redisTemplate.opsForZSet().score(key, value);
    } catch (Exception e) {
      log.error("获取ZSet分数失败, key: {}, value: {}", key, value, e);
      return null;
    }
  }

  /**
   * 增加ZSet中值的分数
   *
   * @param key 键
   * @param value 值
   * @param delta 增量
   * @return 新的分数
   */
  public Double zIncrementScore(String key, Object value, double delta) {
    try {
      return redisTemplate.opsForZSet().incrementScore(key, value, delta);
    } catch (Exception e) {
      log.error("增加ZSet分数失败, key: {}, value: {}, delta: {}", key, value, delta, e);
      return null;
    }
  }

  // =============================  List操作  =============================

  /**
   * 设置List缓存（右侧添加）
   *
   * @param key 键
   * @param values 值集合
   * @return 添加后的列表长度
   */
  public long lRightPush(String key, Object... values) {
    try {
      Long size = redisTemplate.opsForList().rightPushAll(key, values);
      return size != null ? size : 0;
    } catch (Exception e) {
      log.error("设置List缓存失败, key: {}", key, e);
      return 0;
    }
  }

  /**
   * 获取List缓存（范围）
   *
   * @param key 键
   * @param start 起始位置
   * @param end 结束位置
   * @return 值列表
   */
  public List<Object> lRange(String key, long start, long end) {
    try {
      return redisTemplate.opsForList().range(key, start, end);
    } catch (Exception e) {
      log.error("获取List缓存失败, key: {}, start: {}, end: {}", key, start, end, e);
      return null;
    }
  }
}
