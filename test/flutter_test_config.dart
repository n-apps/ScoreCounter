/*
 * Copyright (c) 2022 Score Counter
 */

import 'dart:async';

import 'package:resource_repository/resource_repository.dart';
import 'package:score_counter/data/dto.dart';
import 'package:score_counter/dependencies.dart';

Future<void> testExecutable(FutureOr<void> Function() testMain) async {
  Dependencies.register(allowReassignment: true);
  getIt.registerLazySingleton<CacheStorage<String, CounterDto>>(
    () => MemoryCacheStorage('counters'),
  );
  await testMain();
}
