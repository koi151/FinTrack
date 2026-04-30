package com.koi151.money.fintrack.common.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.koi151.money.fintrack.common.constant.CacheRegion;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching // for @Cacheable, @CachePut,...
public class RedisConfig {

  private static final Duration DEFAULT_TTL = Duration.ofHours(1);

  /**
   * Configures a secure ObjectMapper with polymorphic type validation and Java 8 date support.
   *
   * @return Configured ObjectMapper instance
   */
  @Bean
  public ObjectMapper redisObjectMapper() {
    // Jackson not support Java 8 Date/Time API, so we need to register module
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());

    // Security validator essential for Object.class deserialization
    BasicPolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
      .allowIfSubType("com.koi151.money.fintrack") // Allow our application classes
      .allowIfSubType("java.util") // Allow Lists, Maps
      .allowIfSubType("java.time") // Allow Dates
      .build();

    // auto include @class to JSON when saving to Redis
    mapper.activateDefaultTyping(
      typeValidator,
      ObjectMapper.DefaultTyping.NON_FINAL,
      JsonTypeInfo.As.PROPERTY
    );

    return mapper;
  }

  /**
   * Managed caching strategy for Redis, how to save and specific TTL settings
   *
   * @param connectionFactory Redis connection factory
   * @param redisObjectMapper Custom ObjectMapper for JSON serialization
   * @return Configured RedisCacheManager
   */
  @Bean
  public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory, ObjectMapper redisObjectMapper) {

    JsonRedisSerializer jsonSerializer = new JsonRedisSerializer(redisObjectMapper);

    RedisCacheConfiguration defaultConfig = createDefaultCacheConfig(jsonSerializer);

    Map<String, RedisCacheConfiguration> cacheConfigurations = buildCacheConfigurations(defaultConfig);

    return RedisCacheManager.builder(connectionFactory)
      .cacheDefaults(defaultConfig)
      .withInitialCacheConfigurations(cacheConfigurations)
      .transactionAware()
      .build();
  }

  /**
   * Creates base cache configuration with JSON serialization.
   */
  private RedisCacheConfiguration createDefaultCacheConfig(JsonRedisSerializer jsonSerializer) {

    return RedisCacheConfiguration.defaultCacheConfig()
      .entryTtl(DEFAULT_TTL)
      .disableCachingNullValues()
      .serializeKeysWith( // key of cache will be saved as String
        RedisSerializationContext.SerializationPair.fromSerializer(
          new StringRedisSerializer()
        )
      )
      .serializeValuesWith( // value of cache will be saved as JSON
        RedisSerializationContext.SerializationPair.fromSerializer(
          jsonSerializer
        )
      );
  }

  /**
   * Builds cache configurations from enum definitions.
   */
  private Map<String, RedisCacheConfiguration> buildCacheConfigurations(RedisCacheConfiguration baseConfig) {

    Map<String, RedisCacheConfiguration> configs = new HashMap<>();
    for (CacheRegion region : CacheRegion.values()) {
      configs.put(region.getCacheName(), baseConfig.entryTtl(region.getTtl()));
    }

    return configs;
  }
}