package com.koi151.money.fintrack.common.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * Custom Redis Serializer handles conversion: Java Object <-> JSON Byte Array
 */
public class JsonRedisSerializer implements RedisSerializer<Object> {

  private final ObjectMapper objectMapper;

  public JsonRedisSerializer(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /**
   * Converts Java Object -> JSON Byte Array.
   */
  @Override
  public byte[] serialize(Object t) throws SerializationException {
    try {
      return t == null
        ? new byte[0]
        : objectMapper.writeValueAsBytes(t);

    } catch (JsonProcessingException e) {
      throw new SerializationException("Error serializing object to JSON", e);
    }
  }

  /**
   * Converts JSON Byte Array -> Java Object.
   */
  @Override
  public Object deserialize(byte[] bytes) throws SerializationException {
    try {
      return bytes.length == 0
        ? null
        : objectMapper.readValue(bytes, Object.class);

    } catch (Exception e) {
      throw new SerializationException("Error deserializing JSON to object", e);
    }
  }
}