/*
 * Copyright (c) 2022 Score Counter
 */

import 'package:flutter/material.dart';

//@formatter:off
abstract class _Colors {
  Color get colorPrimary;
  Color get colorPrimaryVariant;
  Color get colorOnPrimary;
  Color get colorSecondary;
  Color get colorSecondaryVariant;
  Color get colorOnSecondary;
  Color get colorError;
  Color get colorOnError;
  Color get textColorPrimary;
  Color get textColorSecondary;
  Color get textColorHint;
  Color get primaryBackground;
  Color get rippleColor;
  Color get sectionHeader;
}
//@formatter:on

class _ColorsDay extends _Colors {
  @override
  final colorPrimary = const Color(0xFF17B5A0);
  @override
  final colorPrimaryVariant = const Color(0xFF118878);
  @override
  final colorOnPrimary = const Color(0xFF000000);
  @override
  final colorSecondary = const Color(0xFF9559D9);
  @override
  final colorSecondaryVariant = const Color(0xFF6B2AB5);
  @override
  final colorOnSecondary = const Color(0xFFffffff);
  @override
  final colorError = const Color(0xFFFF4F5E);
  @override
  final colorOnError = const Color(0xFFffffff);
  @override
  final textColorPrimary = const Color(0xde000000);
  @override
  final textColorSecondary = const Color(0x89000000);
  @override
  final textColorHint = const Color(0x42000000);
  @override
  final primaryBackground = const Color(0xffffffff);
  @override
  final rippleColor = const Color(0x0D08A396);
  @override
  final sectionHeader = const Color(0xffdddddd);
}

class _ColorsNight extends _Colors {
  @override
  final colorPrimary = const Color(0xFF17B5A0);
  @override
  final colorPrimaryVariant = const Color(0xFF118878);
  @override
  final colorOnPrimary = const Color(0xFFffffff);
  @override
  final colorSecondary = const Color(0xFFAF83E2);
  @override
  final colorSecondaryVariant = const Color(0xFF9559D9);
  @override
  final colorOnSecondary = const Color(0xFFffffff);
  @override
  final colorError = const Color(0xFFFF4F5E);
  @override
  final colorOnError = const Color(0xFFffffff);
  @override
  final textColorPrimary = const Color(0xFFFFFFFF);
  @override
  final textColorSecondary = const Color(0xDEFFFFFF);
  @override
  final textColorHint = const Color(0x89FFFFFF);
  @override
  final primaryBackground = const Color(0xff121212);
  @override
  final rippleColor = const Color(0x0D08A396);
  @override
  final sectionHeader = const Color(0xff6B6B6B);
}

class AppTheme {

  static ThemeData? theme;
  static BrandThemeData? brandTheme;

  static ThemeData getTheme(Brightness brightness) {
    if (theme?.brightness != brightness) {
      final colors = brightness == Brightness.light
          ? _ColorsDay()
          : _ColorsNight();
      theme = ThemeData(
        useMaterial3: true,
        brightness: Brightness.light,
        scaffoldBackgroundColor: colors.primaryBackground,
        primaryColor: colors.colorPrimary,
        errorColor: colors.colorError,
        splashColor: colors.rippleColor,
        textTheme: const TextTheme(),
      );
    }
    return theme!;
  }

  static BrandThemeData getBrandTheme(Brightness brightness) {
    if (brandTheme?.brightness != brightness) {
      final colors = brightness == Brightness.light
          ? _ColorsDay()
          : _ColorsNight();
      brandTheme = BrandThemeData(
        brightness: brightness,
        text: BrandTextThemeData(colors.textColorPrimary),
      );
    }
    return brandTheme!;
  }
}


extension BrandThemeDataExt on ThemeData {
  BrandThemeData get brand => AppTheme.getBrandTheme(brightness);
}

class BrandThemeData {
  final Brightness brightness;
  final BrandTextThemeData text;

  const BrandThemeData({
    required this.brightness,
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
