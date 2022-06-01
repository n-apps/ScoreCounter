/*
 * Copyright (c) 2022 Score Counter
 */

import 'package:flutter/foundation.dart';
import 'package:go_router/go_router.dart';
import 'package:score_counter/dependencies.dart';
import 'package:score_counter/feature/main_screen/page.dart';

class AppRouter {
  static GoRouter get() => getIt<GoRouter>();

  static GoRouter create() {
    return GoRouter(
      debugLogDiagnostics: kDebugMode,
      initialLocation: '/',
      routes: [
        GoRoute(path: '/', builder: MainScreenPage.buildRoute),
      ],
    );
  }
}
