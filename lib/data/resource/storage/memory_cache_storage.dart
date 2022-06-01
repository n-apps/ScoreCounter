/*
 * Copyright (c) 2022 Score Counter
 * 2020-2021 NaikSoftware, drstranges, MagTuxGit
 */

import 'package:score_counter/data/resource/cache_storage.dart';
import 'package:synchronized/synchronized.dart';

class SimpleMemoryCacheStorage<K, V> implements CacheStorage<K, V> {
  static final _lock = Lock();
  static final Map<String, Map> _boxes = {};

  final String _boxKey;

  SimpleMemoryCacheStorage(this._boxKey);

  @override
  Future<void> ensureInitialized() => Future.value();

  static Future<void> clearAll() async {
    _boxes.clear();
  }

  Future<Map<K, CacheEntry<V>>> _ensureBox() => _lock.synchronized(() async {
        Map? box = _boxes[_boxKey];
        if (box == null) {
          box = <K, CacheEntry<V>>{};
          _boxes[_boxKey] = box;
        }
        return box as Map<K, CacheEntry<V>>;
      });

  @override
  Future<void> clear() => _ensureBox().then((box) => box.clear());

  @override
  Future<CacheEntry<V>?> get(K cacheKey) async {
    return (await _ensureBox())[cacheKey];
  }

  @override
  Future<void> put(K cacheKey, V data, {int? storeTime}) async {
    final box = await _ensureBox();
    box[cacheKey] = CacheEntry(
      data,
      storeTime: storeTime ?? DateTime.now().millisecondsSinceEpoch,
    );
  }

  @override
  Future<void> delete(K cacheKey) async {
    (await _ensureBox()).remove(cacheKey);
  }

  @override
  Stream<List<V>> watch() => throw UnsupportedError('Not supported!');

  @override
  Future<List<V>> getAll() =>
      _ensureBox().then((box) => box.values.map((e) => e.data).toList());

  @override
  String toString() => 'MemoryCacheStorage($_boxKey)';
}
