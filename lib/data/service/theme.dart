/*
 * Copyright (c) 2022 Score Counter
 */

import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:score_counter/dependencies.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:collection/collection.dart';

extension BrightnessExt on Brightness {
  int get intValue {
    switch (this) {
      case Brightness.light:
        return 0;
      case Brightness.dark:
        return 1;
    }
  }
}

class ThemeService extends ValueNotifier<Brightness> {
  final _ThemePrefs _prefs = _ThemePrefs();

  Brightness get brightness => value;

  factory ThemeService.get() => getIt<ThemeService>();

  ThemeService(Brightness brightness) : super(brightness) {
    _prefs.getThemeBrightness().then((b) {
      value = b ?? brightness;
    });
    final window = WidgetsBinding.instance.window;
    window.onPlatformBrightnessChanged = () {
      final brightness = window.platformBrightness;
      _prefs.getThemeBrightness().then((b) {
        if (b == null) {
          value = brightness; // Not overridden by the user, apply system theme.
        }
      });
    };
  }

  Future<void> setThemeBrightness(Brightness brightness) =>
      _prefs.setThemeBrightness(brightness).then((_) => value = brightness);

  Future<void> toggleThemeBrightness() => setThemeBrightness(
        value == Brightness.dark ? Brightness.light : Brightness.dark,
      );
}

class _ThemePrefs {
  static const keyThemeBrightness = 'keyThemePrefsBrightness';

  Future<void> setThemeBrightness(Brightness brightness) => _prefs()
      .then((prefs) => prefs.setInt(keyThemeBrightness, brightness.intValue));

  Future<Brightness?> getThemeBrightness() => _prefs().then((prefs) {
        final brightnessInt = prefs.getInt(keyThemeBrightness);
        return Brightness.values
            .firstWhereOrNull((it) => it.intValue == brightnessInt);
      });

  Future<SharedPreferences> _prefs() => SharedPreferences.getInstance();
}
