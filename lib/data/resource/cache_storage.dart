/*
 * Copyright (c) 2022 Score Counter
 * 2020-2021 NaikSoftware, drstranges, MagTuxGit
 */

abstract class CacheStorage<K, V> {

  Future<dynamic> ensureInitialized();

  Future<void> clear();

  Future<CacheEntry<V>?> get(K cacheKey);

  Future<void> put(K cacheKey, V data, {int? storeTime});

  Future<void> delete(K cacheKey);

  Stream<List<V>> watch();

  Future<List<V>> getAll();
}

class CacheEntry<V> {
  V data;
  int storeTime;

  CacheEntry(this.data, {required this.storeTime});
}