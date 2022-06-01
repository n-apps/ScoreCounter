/*
 * Copyright (c) 2022 Score Counter
 */
import 'dart:developer';

import 'package:score_counter/dependencies.dart';

class Logger {
  final String tag;

  const Logger(this.tag);

  factory Logger.get(String tag) => getIt.get(param1: tag);

  void e(String text, {Object? error, StackTrace? stackTrace}) {
    log('$tag: $text', error: error, stackTrace: stackTrace);
  }

  void d(String text) {
    log('$tag: $text');
  }
}
