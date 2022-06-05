/*
 * Copyright (c) 2022 Score Counter
 */
import 'dart:convert';
import 'dart:math';

import 'package:collection/collection.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:intl/intl.dart';
import 'package:resource_repository/resource_repository.dart';
import 'package:score_counter/data/dto.dart';
import 'package:score_counter/data/service/theme.dart';
import 'package:score_counter/dependencies.dart';
import 'package:score_counter/styles.dart';
import 'package:worker_manager/worker_manager.dart';

class CountersService {
  final StreamRepository<String, CounterDto> _repository;
  final _random = Random();
  Set<String>? _names;

  CountersService()
      : _repository = StreamRepository<String, CounterDto>.local(
          storage: Dependencies.get<CacheStorage<String, CounterDto>>(),
        );

  factory CountersService.get() => getIt.get();

  Stream<List<CounterDto>> counters() =>
      _repository.watch().map(_prepareCounters);

  Future<bool> add() async {
    final counters = await _repository.getAll().then(_prepareCounters);
    final usedNames = counters.map((c) => c.name);
    final availableNames = await getNames().then((names) => names.toList()
      ..removeWhere(
        (name) => usedNames.contains(name),
      ));
    if (availableNames.isEmpty) return false;

    final colors = AppTheme.getBrandTheme(ThemeService.get().brightness)
        .counterColors
        .toList()
      ..shuffle(_random);

    final lastPosition = counters.lastOrNull?.position ?? -1;
    final counter = CounterDto(
      name: availableNames.toList()[_random.nextInt(availableNames.length)],
      color: colors.first,
      position: lastPosition + 1,
      score: 0,
    );
    await _repository.putValue(counter.name, counter);
    return true;
  }

  Future<void> update(CounterDto counter) =>
      _repository.putValue(counter.name, counter);

  Future<void> delete(CounterDto counter) => _repository.clear(counter.name);

  Future<void> clear() => _repository.clear();

  @protected
  Future<Set<String>> getNames() async {
    if (_names == null) {
      String data;
      try {
        data = await rootBundle
            .loadString('assets/names/${Intl.getCurrentLocale()}.json');
      } on Exception {
        data = await rootBundle.loadString('assets/names/en.json');
      }
      _names = await Executor().execute(arg1: data, fun1: _decodeNames);
    }
    return _names!;
  }

  List<CounterDto> _prepareCounters(List<CounterDto> counters) {
    final c = counters.toList();
    c.sort((a, b) => a.position.compareTo(b.position));
    return c;
  }
}

Set<String> _decodeNames(String data) =>
    json.decode(data).map<String>((value) => value.toString()).toSet();
