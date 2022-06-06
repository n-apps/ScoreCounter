/*
 * Copyright (c) 2022 Score Counter
 */
import 'package:flutter/cupertino.dart';
import 'package:get_it/get_it.dart';
import 'package:go_router/go_router.dart';
import 'package:resource_repository/resource_repository.dart';
import 'package:resource_repository_hive/resource_repository_hive.dart';
import 'package:score_counter/data/dto.dart';
import 'package:score_counter/data/service/logger.dart';
import 'package:score_counter/data/service/prefs.dart';
import 'package:score_counter/data/service/theme.dart';
import 'package:score_counter/routes.dart';

import 'data/service/counters.dart';

final getIt = GetIt.instance;

class Dependencies {
  static void register({bool allowReassignment = false}) {
    getIt.allowReassignment = allowReassignment;

    getIt.registerFactoryParam<Logger, String, void>((tag, _) => Logger(tag));
    getIt.registerLazySingleton<CacheStorage<String, CounterDto>>(
      () => HiveCacheStorage('counters', decode: CounterDto.fromJson),
    );
    getIt.registerLazySingleton<GoRouter>(AppRouter.create);
    getIt.registerLazySingleton<ThemeService>(
      () => ThemeService(WidgetsBinding.instance.window.platformBrightness),
    );
    getIt.registerLazySingleton<PrefsService>(PrefsService.new);
    getIt.registerLazySingleton<CountersService>(CountersService.new);
  }

  static T get<T extends Object>() => getIt();
}
