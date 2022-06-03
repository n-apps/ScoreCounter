/*
 * Copyright (c) 2022 Score Counter
 */
import 'package:flutter/foundation.dart';
import 'package:resource_repository/resource_repository.dart';
import 'package:resource_repository_hive/resource_repository_hive.dart';
import 'package:resource_repository_objectbox/resource_repository_objectbox.dart';
import 'package:score_counter/data/dto.dart';
import 'package:score_counter/dependencies.dart';

class CountersService {
  final StreamRepository<String, CounterDto> _repository;

  CountersService()
      : _repository = StreamRepository<String, CounterDto>.local(
            storage: kIsWeb
                ? HiveCacheStorage('counters',
                    decode: (json) => CounterDto.fromJson(json))
                : ObjectBoxCacheStorage(
                    'counters',
                    decode: (json) => CounterDto.fromJson(json),
                  ));

  factory CountersService.get() => getIt.get();

  Stream<List<CounterDto>> counters() => _repository.watch();

  Future<void> update(CounterDto counter) => add(counter);

  Future<void> add(CounterDto counter) =>
      _repository.putValue(counter.name, counter);

  Future<void> delete(CounterDto counter) => _repository.clear(counter.name);

  Future<void> clear() => _repository.clear();
}
