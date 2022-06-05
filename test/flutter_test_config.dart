/*
 * Copyright (c) 2022 Score Counter
 */

import 'dart:async';

import 'package:score_counter/dependencies.dart';

Future<void> testExecutable(FutureOr<void> Function() testMain) async {
  Dependencies.register(allowReassignment: true);
  await testMain();
}