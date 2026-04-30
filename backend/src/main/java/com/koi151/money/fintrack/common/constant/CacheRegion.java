package com.koi151.money.fintrack.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

/**
 * Defines cache regions with their corresponding names and TTLs.
 */
@Getter
@RequiredArgsConstructor
public enum CacheRegion {

  USER_SYNC("userSyncCache", Duration.ofHours(2)),
  USER_DATA("userDataCache", Duration.ofMinutes(30));

  private final String cacheName;
  private final Duration ttl;
}