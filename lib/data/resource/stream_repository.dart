/*
 * Copyright (c) 2022 Score Counter
 * 2020-2021 NaikSoftware, drstranges, MagTuxGit
 */

import 'dart:async';

import 'package:score_counter/data/resource/cache_storage.dart';
import 'package:score_counter/data/resource/resource.dart';
import 'package:score_counter/data/resource/storage/hive_cache_storage.dart';
import 'package:score_counter/data/resource/storage/objectbox/objectbox_cache_storage_stub.dart'
    if (dart.library.io) 'package:score_counter/data/resource/storage/objectbox/objectbox_cache_storage.dart';
import 'package:score_counter/data/resource/stream_resource.dart';
import 'package:flutter/foundation.dart';
import 'package:rxdart/rxdart.dart';
import 'package:synchronized/synchronized.dart';

class StreamRepository<K, V> {
  final Map<K, StreamResource<K, V>> _resources = {};
  final Future<V> Function(K key, ResourceFetchArguments? arguments)? fetch;
  final CacheDurationResolver<K, V> cacheDurationResolver;
  final Map<K, bool> _firstLoad = {};
  final _lock = Lock();
  final CacheStorage<K, V> storage;

  StreamRepository({
    this.fetch,
    required String storageKey,
    required V Function(dynamic json) decode,
    Duration? cacheDuration,
    CacheDurationResolver<K, V>? cacheDurationResolver,
  })  : storage = kIsWeb
            ? HiveCacheStorage(storageKey, decode: decode)
            : createObjectBoxCacheStorage(storageKey, decode: decode),
        cacheDurationResolver =
            (cacheDurationResolver ?? (k, v) => cacheDuration ?? Duration.zero);

  StreamRepository.create({
    this.fetch,
    required this.storage,
    Duration? cacheDuration,
    CacheDurationResolver<K, V>? cacheDurationResolver,
  }) : cacheDurationResolver =
            (cacheDurationResolver ?? (k, v) => cacheDuration ?? Duration.zero);

  Stream<Resource<V>> stream(
    K key, {
    bool? forceReload,
    void Function(V)? doOnStore,
    ResourceFetchArguments? fetchArguments,
    bool allowEmptyLoading = false,
  }) {
    final force = forceReload ?? _firstLoad[key] ?? true;
    _firstLoad[key] = false;
    return _ensureResource(key)
        .asStream()
        .switchMap((resource) => resource.load(
              forceReload: force,
              doOnStore: doOnStore,
              allowEmptyLoading: allowEmptyLoading,
              fetchArguments: fetchArguments,
            ));
  }

  Future<Resource<V>> load(
    K key, {
    bool? forceReload,
    void Function(V)? doOnStore,
    ResourceFetchArguments? fetchArguments,
    bool allowEmptyLoading = false,
  }) =>
      stream(
        key,
        forceReload: forceReload,
        doOnStore: doOnStore,
        fetchArguments: fetchArguments,
        allowEmptyLoading: allowEmptyLoading,
      ).where((r) => r.isNotLoading).first;

  Future<void> invalidate(K key) =>
      _resources[key]?.invalidate() ?? Future.value();

  Future<void> invalidateAll() =>
      Future.wait(_resources.values.map((r) => r.invalidate()));

  Future<void> updateValue(K key, V? Function(V? value) changeValue,
          {bool notifyOnNull = false}) =>
      _ensureResource(key)
          .then((r) => r.updateValue(changeValue, notifyOnNull: notifyOnNull));

  Future<void> putValue(K key, V value) =>
      _ensureResource(key).then((r) => r.putValue(value));

  Stream<List<V>> watch() => storage.watch();

  Future<List<V>> getAll() => storage.getAll();

  Future<void> clear([K? key]) => _lock.synchronized(() async {
        if (key != null) {
          final resource = _resources[key];
          if (resource != null) {
            await resource.close();
            _resources.remove(key);
          } else {
            await storage.ensureInitialized();
            await storage.delete(key);
          }
        } else {
          _resources.clear();
          await storage.clear();
        }
      });

  Future<StreamResource<K, V>> _ensureResource(K key) =>
      _lock.synchronized(() async {
        var resource = _resources[key];
        if (resource == null) {
          resource = StreamResource<K, V>(
            fetch,
            key,
            cacheDurationResolver,
            storage,
          );
          _resources[key] = resource;
        }
        return resource;
      });
}
