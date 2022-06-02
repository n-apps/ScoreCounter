/*
 * Copyright (c) 2022 Score Counter
 * 2020-2021 NaikSoftware, drstranges, MagTuxGit
 */

import 'dart:convert';

import 'package:resource_repository/resource_repository.dart';
import 'package:score_counter/data/service/logger.dart';
import 'package:objectbox/objectbox.dart';
import 'package:rxdart/rxdart.dart';
import 'package:score_counter/generated/objectbox.g.dart';
import 'package:synchronized/synchronized.dart';
import 'package:worker_manager/worker_manager.dart';

// Override global function for conditional import
CacheStorage<K, V> createObjectBoxCacheStorage<K, V>(
  //ignore: avoid-unused-parameters
  String boxKey, {
  //ignore: avoid-unused-parameters
  required V Function(dynamic json) decode,
}) =>
    _ObjectBoxCacheStorage(boxKey, decode: decode);

class _ObjectBoxCacheStorage<K, V> implements CacheStorage<K, V> {
  static Future<Store>? _initStoreTask;
  static final _lock = Lock();

  final _logger = const Logger('ObjectBoxCacheStorage');
  final String _boxKey;
  final Future<String> Function(V value) _encode;
  final Future<V> Function(String data) _decode;

  _ObjectBoxCacheStorage(
    this._boxKey, {
    required V Function(dynamic json) decode,
  })  : _encode =
            ((value) => Executor().execute(arg1: value, fun1: _jsonEncode)),
        _decode = ((data) =>
            Executor().execute(arg1: data, fun1: _jsonDecode).then(decode));

  @override
  Future<Store> ensureInitialized() => _lock.synchronized(_initStore);

  @override
  Future<void> clear() => _ensureBox().then((box) {
        final query = box.query(CacheBoxEntry_.name.equals(_boxKey)).build();
        final count = query.remove();
        query.close();
        _logger.d('From $_boxKey removed all $count');
      });

  @override
  Future<CacheEntry<V>?> get(K key) async {
    final cacheKey = _resolveCacheKey(key);
    CacheBoxEntry? cached = await _findById(cacheKey);
    if (cached != null) {
      try {
        final value = await _decode(cached.data);
        return CacheEntry(value, storeTime: cached.storeTime);
      } catch (e, trace) {
        _logger.e(
          'On load resource $_boxKey by key: $key',
          error: e,
          stackTrace: trace,
        );
      }
    }
    return null;
  }

  @override
  Future<void> put(K key, V data, {int? storeTime}) async {
    final cacheKey = _resolveCacheKey(key);
    final box = await _ensureBox();
    final dataEncoded = await _encode(data);
    final time = storeTime ?? DateTime.now().millisecondsSinceEpoch;
    final exists = await _findById(cacheKey, box);
    if (exists != null) {
      // _logger.d('Update $_boxKey => $cacheKey');
      await box.putAsync(exists
        ..data = dataEncoded
        ..storeTime = time);
    } else {
      // _logger.d('Insert $_boxKey => $cacheKey');
      await box.putAsync(CacheBoxEntry(
        name: _boxKey,
        key: cacheKey,
        data: dataEncoded,
        storeTime: time,
      ));
    }
  }

  @override
  Future<void> delete(K key) async {
    final cacheKey = _resolveCacheKey(key);
    final query = await _findByIdQuery(cacheKey);
    query.remove();
    query.close();
  }

  @override
  Stream<List<V>> watch() {
    return _ensureBox().asStream().switchMap((box) => box
        .query(CacheBoxEntry_.name.equals(_boxKey))
        .watch(triggerImmediately: true)
        .map((query) => query.find())
        .switchMap(
          (entries) => Stream.fromFutures(entries.map((e) => _decode(e.data)))
              .toList()
              .asStream(),
        ));
  }

  @override
  Future<List<V>> getAll() => _ensureBox()
      .then((box) =>
          box.query(CacheBoxEntry_.name.equals(_boxKey)).build().find())
      .then((entries) =>
          Stream.fromFutures(entries.map((e) => _decode(e.data))).toList());

  Future<CacheBoxEntry?> _findById(
    String cacheKey, [
    Box<CacheBoxEntry>? box,
  ]) async {
    final query = await _findByIdQuery(cacheKey, box);
    final cached = query.findFirst();
    query.close();
    return cached;
  }

  Future<Query<CacheBoxEntry>> _findByIdQuery(
    String cacheKey, [
    Box<CacheBoxEntry>? box,
  ]) async {
    return (box ?? (await _ensureBox()))
        .query(CacheBoxEntry_.name
            .equals(_boxKey)
            .and(CacheBoxEntry_.key.equals(cacheKey)))
        .build();
  }

  String _resolveCacheKey(K cacheKey) => cacheKey.toString();

  Future<Box<CacheBoxEntry>> _ensureBox() => ensureInitialized().then(
        (s) => s.box<CacheBoxEntry>(),
      );

  // static Future<void> clearAll() async {
  //   final store = await _lock.synchronized(_initStore);
  //   store.box<CacheBoxEntry>().removeAll();
  // }

  static Future<Store> _initStore() async {
    var task = _initStoreTask;
    task ??= _initStoreTask = openStore();
    return task;
  }

  @override
  String toString() => 'ObjectBoxCacheStorage($_boxKey)';
}

@Entity(uid: 1)
class CacheBoxEntry {
  @Id()
  int internalId = 0;

  @Index()
  String name;

  @Index()
  String key;

  String data;
  int storeTime;

  CacheBoxEntry({
    required this.name,
    required this.key,
    required this.storeTime,
    required this.data,
  });
}

String _jsonEncode<V>(V value) {
  return json.encode(value);
}

dynamic _jsonDecode(String data) {
  return json.decode(data);
}
