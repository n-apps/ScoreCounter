/*
 * Copyright (c) 2022 Score Counter
 * 2020-2021 NaikSoftware, drstranges, MagTuxGit
 */

import 'dart:async';

import 'package:score_counter/data/service/logger.dart';
import 'package:score_counter/data/resource/cache_storage.dart';
import 'package:score_counter/data/resource/resource.dart';
import 'package:rxdart/rxdart.dart';
import 'package:synchronized/synchronized.dart';

class StreamResource<K, V> {
  final _logger = const Logger('StreamResource');
  final BehaviorSubject<Resource<V>> _subject = BehaviorSubject<Resource<V>>();
  final CacheStorage<K, V> _storage;
  final Future<V> Function(K key, ResourceFetchArguments? arguments)? fetch;
  final K resourceKey;
  final CacheDurationResolver<K, V> cacheDurationResolver;

  final _lock = Lock();
  bool _isLoading = false;
  bool _shouldReload = false;

  StreamResource(
    this.fetch,
    this.resourceKey,
    this.cacheDurationResolver,
    this._storage,
  );

  /// forceReload - reload even if cache is valid
  /// allowEmptyLoading - put empty loading to prevent previous SUCCESS to return
  Stream<Resource<V>> load({
    bool forceReload = false,
    void Function(V)? doOnStore,
    bool allowEmptyLoading = false,
    final ResourceFetchArguments? fetchArguments,
  }) {
    if (!_isLoading) {
      _isLoading = true;
      _lock.synchronized(() async {
        _shouldReload = false;
        // try always starting with loading value
        if (allowEmptyLoading || _subject.hasValue) {
          // prevent previous SUCCESS to return
          _subject.add(Resource.loading(_subject.valueOrNull?.data));
        }
        await _loadProcess(forceReload, doOnStore, fetchArguments);
      }).then((_) {
        _isLoading = false;
        if (_shouldReload) {
          load(
            forceReload: forceReload,
            doOnStore: doOnStore,
            allowEmptyLoading: allowEmptyLoading,
          );
        }
      });
    } else if (forceReload) {
      // don't need to call load many times
      // perform another load only once
      _shouldReload = true;
    }
    return _subject;
  }

  Future<void> _loadProcess(
    bool forceReload,
    void Function(V)? doOnStore,
    ResourceFetchArguments? fetchArguments,
  ) async {
    // fetch value from DB
    final cached = await _storage.get(resourceKey);
    if (cached != null) {
      final cacheDuration =
          cacheDurationResolver(resourceKey, cached.data).inMilliseconds;

      if (cached.storeTime <
          DateTime.now().millisecondsSinceEpoch - cacheDuration) {
        forceReload = true;
      }
    }

    if (cached != null || fetch == null) {
      if (forceReload && fetch != null) {
        final resource = Resource.loading(cached?.data);
        if (_subject.valueOrNull != resource) {
          _subject.add(resource);
        }
      } else {
        final resource = Resource.success(cached?.data);
        if (_subject.valueOrNull != resource) {
          _subject.add(resource);
        }
        return;
      }
    }

    // no need to perform another load while fetch not called yet
    _shouldReload = false;

    // fetch value from network
    return _subject.addStream(fetch!(resourceKey, fetchArguments)
        .asStream()
        .asyncMap((data) async {
          if (doOnStore != null) {
            doOnStore(data);
          }
          await _storage.put(resourceKey, data);
          return data;
        })
        .map((data) => Resource.success(data))
        .doOnError((error, trace) => _logger.e(
            'Error loading resource by id $resourceKey with storage $_storage',
            error: error,
            stackTrace: trace))
        .onErrorReturnWith((error, trace) => Resource.error(
            'Resource $resourceKey loading error',
            error: error,
            data: cached?.data)));
  }

  Future<void> updateValue(V? Function(V? value) changeValue,
      {bool notifyOnNull = false}) async {
    _lock.synchronized(() async {
      final cached = await _storage.get(resourceKey);
      final newValue = changeValue.call(cached?.data);

      if (newValue != null) {
        await _storage.put(
          resourceKey,
          newValue,
          storeTime: cached?.storeTime ?? 0,
        );

        _subject.add(Resource.success(newValue));
      } else if (cached != null) {
        await _storage.delete(resourceKey);
        if (notifyOnNull) _subject.add(Resource.success(null));
      }
    });
  }

  Future<void> putValue(V value) async {
    assert(value != null);

    _lock.synchronized(() async {
      await _storage.put(resourceKey, value);
      _subject.add(Resource.success(value));
    });
  }

  Future<void> _clearCache() async {
    await _storage.ensureInitialized();
    await _storage.delete(resourceKey);
  }

  Future<void> _resetStoreTime() async {
    _lock.synchronized(() async {
      var cached = await _storage.get(resourceKey);
      if (cached != null) {
        await _storage.put(resourceKey, cached.data, storeTime: 0);
      } else {
        await _storage.delete(resourceKey);
      }
    });
  }

  Future<void> invalidate() async {
    // don't clear cache for offline usage
    //await _clearCache();
    await _resetStoreTime();
    await load(forceReload: true).where((event) => event.isNotLoading).first;
  }

  Future<void> close() async {
    await _clearCache();
    _subject.close(); // TODO: Maybe leave not closed? Need tests
  }
}

class ResourceFetchArguments {
  final int? limit;
  final String? permissionToken;
  final dynamic payload;

  const ResourceFetchArguments({
    this.limit,
    this.permissionToken,
    this.payload,
  });

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is ResourceFetchArguments &&
          runtimeType == other.runtimeType &&
          permissionToken == other.permissionToken;

  @override
  int get hashCode => permissionToken.hashCode;
}

typedef CacheDurationResolver<K, V> = Duration Function(K key, V value);
