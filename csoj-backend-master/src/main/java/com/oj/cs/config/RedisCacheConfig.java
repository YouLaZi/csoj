package com.oj.cs.config;

import java.time.Duration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;

/** Redis缓存配置 */
@Configuration
@EnableCaching
public class RedisCacheConfig {

  /** 缓存过期时间配置（秒） */
  private static final long CACHE_TTL_USER = 30 * 60; // 用户信息：30分钟

  private static final long CACHE_TTL_QUESTION = 60 * 60; // 题目详情：1小时
  private static final long CACHE_TTL_QUESTION_LIST = 10 * 60; // 题目列表：10分钟
  private static final long CACHE_TTL_HOT = 5 * 60; // 热点数据：5分钟

  /** 配置RedisTemplate 使用Jackson序列化器，提高可读性 */
  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);

    // 使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
    Jackson2JsonRedisSerializer<Object> serializer =
        new Jackson2JsonRedisSerializer<>(Object.class);

    ObjectMapper mapper = new ObjectMapper();
    mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
    mapper.activateDefaultTyping(
        LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
    serializer.setObjectMapper(mapper);

    StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

    // key采用String的序列化方式
    template.setKeySerializer(stringRedisSerializer);
    // hash的key也采用String的序列化方式
    template.setHashKeySerializer(stringRedisSerializer);
    // value序列化方式采用jackson
    template.setValueSerializer(serializer);
    // hash的value序列化方式采用jackson
    template.setHashValueSerializer(serializer);

    template.afterPropertiesSet();
    return template;
  }

  /** 配置CacheManager 支持Spring Cache注解（@Cacheable、@CacheEvict等） */
  @Bean
  public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
    // 配置序列化
    Jackson2JsonRedisSerializer<Object> serializer =
        new Jackson2JsonRedisSerializer<>(Object.class);
    ObjectMapper mapper = new ObjectMapper();
    mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
    mapper.activateDefaultTyping(
        LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
    serializer.setObjectMapper(mapper);

    // 配置序列化
    RedisCacheConfiguration config =
        RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1)) // 默认缓存1小时
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new StringRedisSerializer()))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(serializer))
            .disableCachingNullValues(); // 不缓存null值

    // 针对不同cacheName设置不同的过期时间
    return RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(config)
        .withCacheConfiguration("user", config.entryTtl(Duration.ofSeconds(CACHE_TTL_USER)))
        .withCacheConfiguration("question", config.entryTtl(Duration.ofSeconds(CACHE_TTL_QUESTION)))
        .withCacheConfiguration(
            "questionList", config.entryTtl(Duration.ofSeconds(CACHE_TTL_QUESTION_LIST)))
        .withCacheConfiguration("hotData", config.entryTtl(Duration.ofSeconds(CACHE_TTL_HOT)))
        .transactionAware()
        .build();
  }

  /** 缓存名称常量 */
  public static class CacheNames {
    /** 用户信息缓存 */
    public static final String USER = "user";

    /** 题目详情缓存 */
    public static final String QUESTION = "question";

    /** 题目列表缓存 */
    public static final String QUESTION_LIST = "questionList";

    /** 热点数据缓存 */
    public static final String HOT_DATA = "hotData";

    /** 用户提交记录缓存 */
    public static final String USER_SUBMIT = "userSubmit";

    /** 排行榜缓存 */
    public static final String RANKING = "ranking";
  }
}
