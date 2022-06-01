/*
 * Copyright (c) 2022 Score Counter
 */
import 'package:score_counter/data/dto.dart';
import 'package:score_counter/data/resource/stream_repository.dart';
import 'package:score_counter/dependencies.dart';

class CountersService {
  final StreamRepository<String, CounterDto> _repository;

  CountersService()
      : _repository = StreamRepository<String, CounterDto>(
            storageKey: 'counters',
            decode: (json) => CounterDto.fromJson(json));

  factory CountersService.get() => getIt.get();

  Stream<List<CounterDto>> counters() => _repository.watch();

  Future<void> update(CounterDto counter) => add(counter);

  Future<void> add(CounterDto counter) =>
      _repository.putValue(counter.name, counter);

  Future<void> delete(CounterDto counter) =>
      _repository.clear(counter.name);

  Future<void> clear() => _repository.clear();
}