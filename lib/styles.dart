/*
 * Copyright (c) 2022 Score Counter
 */

import 'package:flutter/cupertino.dart';

class _Colors {
  static const notBlackBlack = Color(0xFF494847);
  static const grayish = Color(0xFFF0F1F4);
  static const windowBg = Color(0xFFF8F6F3);
  static const white = Color(0xFFFFFFFF);
  static const black = Color(0xFF000000);
  static const brandBlack = Color(0xFF1B1B1B);
  static const vegs = Color(0xFFA2D8A1);
  static const notGray = Color(0xFFC9C6C3);
  static const gray = Color(0xFFAFB0B4);
  static const error = Color(0xFFEF4444);
}

final themeLight = CupertinoThemeData(
  brightness: Brightness.light,
  scaffoldBackgroundColor: _Colors.windowBg,
  primaryColor: _Colors.brandBlack,
  barBackgroundColor: _Colors.white,
  textTheme: CupertinoTextThemeData(
    navActionTextStyle: _brandThemeLight.text.secondaryText,
  ),
);

final _brandThemeLight = BrandThemeData(
  colorBackground: _Colors.white,
  colorVegs: _Colors.vegs,
  colorBlack: _Colors.black,
  colorBrandBlack: _Colors.brandBlack,
  colorNotGray: _Colors.notGray,
  colorNotBlackBlack: _Colors.notBlackBlack,
  colorGrayish: _Colors.grayish,
  colorGray: _Colors.gray,
  colorError: _Colors.error,
  text: BrandTextThemeData(_Colors.black, _Colors.brandBlack),
);

const themeDark = CupertinoThemeData(
  brightness: Brightness.dark,
  scaffoldBackgroundColor: _Colors.brandBlack,
  primaryColor: _Colors.vegs,
  barBackgroundColor: _Colors.black,
);

final _brandThemeDark = BrandThemeData(
  colorBackground: _Colors.black,
  colorVegs: _Colors.vegs,
  colorBlack: _Colors.white,
  colorBrandBlack: _Colors.black,
  colorNotGray: _Colors.notGray,
  colorNotBlackBlack: _Colors.windowBg,
  colorGrayish: _Colors.grayish,
  colorGray: _Colors.gray,
  colorError: _Colors.error,
  text: BrandTextThemeData(_Colors.white, _Colors.grayish),
);

class BrandThemeData {
  final Color colorBackground;
  final Color colorVegs;
  final Color colorBrandBlack;
  final Color colorBlack;
  final Color colorNotGray;
  final Color colorNotBlackBlack;
  final Color colorGrayish;
  final Color colorGray;
  final Color colorError;
  final BrandTextThemeData text;

  const BrandThemeData({
    required this.colorBackground,
    required this.colorVegs,
    required this.colorBlack,
    required this.colorBrandBlack,
    required this.colorNotGray,
    required this.colorNotBlackBlack,
    required this.colorGrayish,
    required this.colorGray,
    required this.colorError,
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
  final TextStyle secondaryAccent;
  final TextStyle labels;
  final TextStyle captions;

  BrandTextThemeData(Color deepBlack, Color black)
      : h1 = TextStyle(
    color: deepBlack,
    fontSize: 30,
    fontWeight: FontWeight.w700,
    height: 1.3,
  ),
        h2 = TextStyle(
          color: deepBlack,
          fontSize: 24,
          fontWeight: FontWeight.w500,
          height: 1.3,
        ),
        h3 = TextStyle(
          color: black,
          fontSize: 18,
          fontWeight: FontWeight.w500,
          height: 1.3,
        ),
        button = TextStyle(
          color: black,
          fontSize: 16,
          fontWeight: FontWeight.w500,
          height: 1.3,
        ),
        mainText = TextStyle(
          color: black,
          fontSize: 16,
          fontWeight: FontWeight.w400,
          height: 1.3,
        ),
        secondaryText = TextStyle(
          color: black,
          fontSize: 14,
          fontWeight: FontWeight.w400,
          height: 1.3,
        ),
        secondaryAccent = TextStyle(
          color: black,
          fontSize: 14,
          fontWeight: FontWeight.w500,
          height: 1.3,
        ),
        labels = TextStyle(
          color: black,
          fontSize: 12,
          fontWeight: FontWeight.w400,
          height: 1.3,
        ),
        captions = TextStyle(
          color: black,
          fontSize: 10,
          fontWeight: FontWeight.w400,
          height: 1.3,
        );
}

extension BrandThemeDataExt on CupertinoThemeData {
  BrandThemeData get brand =>
      brightness == Brightness.light ? _brandThemeLight : _brandThemeDark;
}

