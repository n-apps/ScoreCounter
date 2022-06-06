/*
 * Copyright (c) 2022 Score Counter
 */

import 'dart:async';

import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_crashlytics/firebase_crashlytics.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:score_counter/dependencies.dart';
import 'package:score_counter/firebase_options.dart';
import 'package:worker_manager/worker_manager.dart';

@visibleForTesting
// ignore: avoid-global-state
bool isIntegrationTest = false;

Future<void> initialize() async {
  WidgetsFlutterBinding.ensureInitialized();

  unawaited(SystemChrome.setEnabledSystemUIMode(SystemUiMode.edgeToEdge));

  if (!isIntegrationTest) {
    await _initFirebase();
  }

  // Prepare isolates pool (threads pool).
  await Executor().warmUp();

  Dependencies.register();
}

Future<void> _initFirebase() async {
  await Firebase.initializeApp(options: DefaultFirebaseOptions.currentPlatform);
  // Pass all uncaught errors from the framework to Crashlytics.
  FlutterError.onError = FirebaseCrashlytics.instance.recordFlutterFatalError;
}
