/*
 * Copyright (c) 2022 Score Counter
 */

import 'package:flutter/material.dart';

class _Colors {
  static const colorPrimary = Color(0xFF17B5A0);
  static const colorPrimaryVariant = Color(0xFF118878);
  static const colorOnPrimary = Color(0xFF000000);
  static const colorSecondary = Color(0xFF9559D9);
  static const colorSecondaryVariant = Color(0xFF6B2AB5);
  static const colorOnSecondary = Color(0xFFffffff);
  static const colorError = Color(0xFFFF4F5E);
  static const colorOnError = Color(0xFFffffff);
  static const textColorPrimary = Color(0xde000000);
  static const textColorSecondary = Color(0x89000000);
  static const textColorHint = Color(0x42000000);
  static const primaryBackground = Color(0xffffffff);
  static const rippleColor = Color(0x0D08A396);
  static const sectionHeader = Color(0xffdddddd);
}

class _ColorsNight {
  static const colorPrimary = Color(0xFF17B5A0);
  static const colorPrimaryVariant = Color(0xFF118878);
  static const colorOnPrimary = Color(0xFFffffff);
  static const colorSecondary = Color(0xFFAF83E2);
  static const colorSecondaryVariant = Color(0xFF9559D9);
  static const colorOnSecondary = Color(0xFFffffff);
  static const colorError = Color(0xFFFF4F5E);
  static const colorOnError = Color(0xFFffffff);
  static const textColorPrimary = Color(0xFFFFFFFF);
  static const textColorSecondary = Color(0xDEFFFFFF);
  static const textColorHint = Color(0x89FFFFFF);
  static const primaryBackground = Color(0xff121212);
  static const rippleColor = Color(0x0D08A396);
  static const sectionHeader = Color(0xff6B6B6B);
}

class AppTheme {
  static final themeLight = ThemeData(
    useMaterial3: true,
    brightness: Brightness.light,
    primaryColor: _Colors.colorPrimary,
    errorColor: _Colors.colorError,
    splashColor: _Colors.rippleColor,
    textTheme: const TextTheme(),
  );

  static final _brandThemeLight = BrandThemeData(
    text: BrandTextThemeData(_Colors.textColorPrimary),
  );

  static final themeDark = ThemeData(
    useMaterial3: true,
    brightness: Brightness.dark,
    primaryColor: _ColorsNight.colorPrimary,
  );

  static final _brandThemeDark = BrandThemeData(
    text: BrandTextThemeData(_ColorsNight.textColorPrimary),
  );
}

class BrandThemeData {
  final BrandTextThemeData text;

  const BrandThemeData({
    required this.text,
  });
}

class BrandTextThemeData {
  final TextStyle h1;
  final TextStyle h2;
  final TextStyle h3;
  final TextStyle button;
  final TextStyle mainText;
  final TextStyle secondaryText;
  final TextStyle labels;
  final TextStyle captions;

  BrandTextThemeData(Color textPrimaryColor)
      : h1 = TextStyle(
          color: textPrimaryColor,
          fontSize: 30,
          fontWeight: FontWeight.w700,
          height: 1.3,
        ),
        h2 = TextStyle(
          color: textPrimaryColor,
          fontSize: 24,
          fontWeight: FontWeight.w500,
          height: 1.3,
        ),
        h3 = TextStyle(
          color: textPrimaryColor,
          fontSize: 18,
          fontWeight: FontWeight.w500,
          height: 1.3,
        ),
        button = TextStyle(
          color: textPrimaryColor,
          fontSize: 16,
          fontWeight: FontWeight.w500,
          height: 1.3,
        ),
        mainText = TextStyle(
          color: textPrimaryColor,
          fontSize: 16,
          fontWeight: FontWeight.w400,
          height: 1.3,
        ),
        secondaryText = TextStyle(
          color: textPrimaryColor,
          fontSize: 14,
          fontWeight: FontWeight.w400,
          height: 1.3,
        ),
        labels = TextStyle(
          color: textPrimaryColor,
          fontSize: 12,
          fontWeight: FontWeight.w400,
          height: 1.3,
        ),
        captions = TextStyle(
          color: textPrimaryColor,
          fontSize: 10,
          fontWeight: FontWeight.w400,
          height: 1.3,
        );
}

extension BrandThemeDataExt on ThemeData {
  BrandThemeData get brand => brightness == Brightness.light
      ? AppTheme._brandThemeLight
      : AppTheme._brandThemeDark;
}
