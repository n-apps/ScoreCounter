/*
 * Copyright (c) 2022 Score Counter
 */

import 'package:flutter/cupertino.dart';
import 'package:get_it/get_it.dart';
import 'package:go_router/go_router.dart';
import 'package:score_counter/data/service/prefs.dart';
import 'package:score_counter/data/service/theme.dart';
import 'package:score_counter/routes.dart';

final getIt = GetIt.instance;

class Dependencies {
  static void register({bool allowReassignment = false}) {
    getIt.allowReassignment = allowReassignment;

    getIt.registerLazySingleton<GoRouter>(() => AppRouter.create());

    getIt.registerLazySingleton<ThemeService>(
      () => ThemeService(WidgetsBinding.instance.window.platformBrightness),
    );

    getIt.registerLazySingleton<PrefsService>(() => PrefsService());
  }
}
