

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:score_counter/bootstrap.dart';
import 'package:score_counter/data/service/theme.dart';
import 'package:score_counter/generated/l10n.dart';
import 'package:score_counter/routes.dart';
import 'package:score_counter/styles.dart';

void main() => initialize().then((_) => runApp(const MyApp()));

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final router = AppRouter.get();
    return ValueListenableBuilder<Brightness>(
      valueListenable: ThemeService.get(),
      builder:(_, brightness, __) => MaterialApp.router(
        theme: AppTheme.getTheme(brightness),
        routeInformationParser: router.routeInformationParser,
        routerDelegate: router.routerDelegate,
        localizationsDelegates: const [
          S.delegate,
          DefaultWidgetsLocalizations.delegate,
          DefaultCupertinoLocalizations.delegate,
          DefaultMaterialLocalizations.delegate,
        ],
        supportedLocales: S.delegate.supportedLocales,
      ),
    );
  }
}