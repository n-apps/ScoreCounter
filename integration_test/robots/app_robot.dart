/*
 * Copyright (c) 2022 Score Counter
 */
import 'dart:io';
import 'dart:ui';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';
import 'package:integration_test/src/channel.dart';

class AppRobot {
  final IntegrationTestWidgetsFlutterBinding binding;
  final WidgetTester tester;

  AppRobot(this.binding, this.tester);

  Future<void> takeScreenshot(String name) async {
    final platformName = !kIsWeb
        ? Platform.isAndroid
            ? 'android'
            : 'ios'
        : 'web';

    final screenshotName = '$platformName-$name';
    if (platformName == 'android') {
      await _takeScreenshotAndroid(screenshotName);
    } else {
      await binding.takeScreenshot(screenshotName);
    }
  }

  Future<void> _takeScreenshotAndroid(String name) async {
    // TODO: Change to binding.convertFlutterSurfaceToImage() when this issue is fixed: https://github.com/flutter/flutter/issues/92381.
    await integrationTestChannel.invokeMethod<void>(
      'convertFlutterSurfaceToImage',
      null,
    );
    await tester.pumpAndSettle();

    binding.reportData ??= <String, dynamic>{};
    binding.reportData?['screenshots'] ??= <dynamic>[];
    integrationTestChannel.setMethodCallHandler((MethodCall call) async {
      switch (call.method) {
        case 'scheduleFrame':
          PlatformDispatcher.instance.scheduleFrame();
          break;
      }
      return null;
    });
    final rawBytes = await integrationTestChannel.invokeMethod<List<int>>(
      'captureScreenshot',
      <String, dynamic>{'name': name},
    );
    if (rawBytes == null) {
      throw StateError(
        'Expected a list of bytes, but instead captureScreenshot returned null',
      );
    }
    final data = <String, dynamic>{
      'screenshotName': name,
      'bytes': rawBytes,
    };
    assert(data.containsKey('bytes'));
    (binding.reportData?['screenshots'] as List<dynamic>).add(data);

    await integrationTestChannel.invokeMethod<void>(
      'revertFlutterImage',
      null,
    );
  }
}
