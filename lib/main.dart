

import 'package:flutter/material.dart';
import 'package:score_counter/bootstrap.dart';
import 'package:score_counter/routes.dart';

void main() => initialize().then((_) => runApp(const MyApp()));

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  // This widget is the root of application.
  @override
  Widget build(BuildContext context) {
    final router = AppRouter.get();
    return MaterialApp.router(
      routeInformationParser: router.routeInformationParser,
      routerDelegate: router.routerDelegate,
    );
  }
}