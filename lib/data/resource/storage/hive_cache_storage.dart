/*
 * Copyright (c) 2022 Score Counter
 * 2020-2021 NaikSoftware, drstranges, MagTuxGit
 */

import 'dart:convert';

import 'package:resource_repository/resource_repository.dart';
import 'package:score_counter/data/service/logger.dart';
import 'package:hive_flutter/hive_flutter.dart';
import 'package:rxdart/rxdart.dart';
import 'package:synchronized/synchronized.dart';
import 'package:worker_manager/worker_manager.dart';

class HiveCacheStorage<K, V> implements CacheStorage<K, V> {
  static Future<void>? _openDbTask;
  static final _lock = Lock();
  static final Map<String, Box<_BoxCacheEntry>> _boxes = {};

  final _logger = const Logger('BoxCacheStorage');
  final String _boxKey;
  final Future<String> Function(V value) _encode;
  final Future<V> Function(String data) _decode;

  HiveCacheStorage(
    this._boxKey, {
    required V Function(dynamic json) decode,
  })  : _encode =
            ((value) => Executor().execute(arg1: value, fun1: _jsonEncode)),
        _decode = ((data) =>
            Executor().execute(arg1: data, fun1: _jsonDecode).then(decode));

  @override
  Future<void> ensureInitialized() => _lock.synchronized(_initHive);

  static Future<void> clearAll() async {
    await _lock.synchronized(_initHive);
    await Hive.deleteFromDisk();
  }

  static Future<void> _initHive() {
    var task = _openDbTask;
    if (task == null) {
      Hive.registerAdapter(CacheEntryAdapter());
      task = _openDbTask = Hive.initFlutter();
    }
    return task;
  }

  Future<Box<_BoxCacheEntry>> _ensureBox() => _lock.synchronized(() async {
        Box<_BoxCacheEntry>? box = _boxes[_boxKey];
        if (box == null) {
          try {
            await _initHive();
            box = await Hive.openBox(_boxKey);
          } catch (e, trace) {
            _logger.e('Open box $_boxKey error', error: e, stackTrace: trace);
            Hive.deleteBoxFromDisk(_boxKey);
            box = await Hive.openBox(_boxKey);
          }
          _boxes[_boxKey] = box;
        }
        return box;
      });

  @override
  Future<void> clear() => _ensureBox().then((box) => box.clear());

  @override
  Future<CacheEntry<V>?> get(K cacheKey) async {
    final boxKey = _resolveBoxKey(cacheKey);
    final cached = (await _ensureBox()).get(boxKey);
    if (cached != null) {
      try {
        final value = await _decode(cached.data);
        return CacheEntry(value, storeTime: cached.storeTime);
      } catch (e, trace) {
        _logger.e('On load resource by key: $boxKey',
            error: e, stackTrace: trace);
      }
    }
    return null;
  }

  String _resolveBoxKey(K cacheKey) => cacheKey.toString();

  @override
  Future<void> put(K cacheKey, V data, {int? storeTime}) async {
    final boxKey = _resolveBoxKey(cacheKey);
    await (await _ensureBox()).put(
      boxKey,
      _BoxCacheEntry(
        await _encode(data),
        storeTime: storeTime ?? DateTime.now().millisecondsSinceEpoch,
      ),
    );
  }

  @override
  Future<void> delete(K cacheKey) async {
    final boxKey = _resolveBoxKey(cacheKey);
    await (await _ensureBox()).delete(boxKey);
  }

  @override
  Stream<List<V>> watch() => _ensureBox().asStream().switchMap((box) => box
      .watch()
      .startWith(BoxEvent('First load', null, false))
      .switchMap((event) =>
          Stream.fromFutures(box.values.map((e) => _decode(e.data)))
              .toList()
              .asStream()));

  @override
  Future<List<V>> getAll() async {
    final valuesRaw = await _ensureBox().then((value) => value.values);
    final List<V> values = [];
    for (final value in valuesRaw) {
      values.add(await _decode(value.data));
    }
    return values;
  }

  @override
  String toString() => 'HiveCacheStorage($_boxKey)';
}

class _BoxCacheEntry {
  String data;
  int storeTime;

  _BoxCacheEntry(this.data, {required this.storeTime});
}

class CacheEntryAdapter extends TypeAdapter<_BoxCacheEntry> {
  @override
  final typeId = 0;

  @override
  _BoxCacheEntry read(BinaryReader reader) {
    return _BoxCacheEntry(reader.read(), storeTime: reader.read());
  }

  @override
  void write(BinaryWriter writer, _BoxCacheEntry obj) {
    writer.write(obj.data);
    writer.write(obj.storeTime);
  }
}

String _jsonEncode<V>(V value) {
  return json.encode(value);
}

dynamic _jsonDecode(String data) {
  return json.decode(data);
}
