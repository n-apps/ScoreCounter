/*
 * Copyright (c) 2022 Score Counter
 */
import 'dart:developer';

class Logger {
  final String tag;

  const Logger(this.tag);

  void e(String text, {Object? error, StackTrace? stackTrace}) {
    log('$tag: $text', error: error, stackTrace: stackTrace);
  }

  void d(String text) {
    log('$tag: $text');
  }
}
