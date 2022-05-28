/*
 * Copyright (c) 2022 Score Counter
 */

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import 'package:score_counter/feature/main_screen/bloc.dart';
import 'package:score_counter/generated/assets.gen.dart';
import 'package:score_counter/generated/l10n.dart';

enum NavigationTab {
  counters,
  dice,
  more,
}

class MainScreenPage extends StatefulWidget {
  static Widget buildRoute(BuildContext _, GoRouterState __) =>
      BlocProvider<MainScreenBloc>(
        create: (context) => MainScreenBloc(),
        child: const MainScreenPage(),
      );

  const MainScreenPage({Key? key}) : super(key: key);

  @override
  State<MainScreenPage> createState() => _MainScreenPageState();
}

class _MainScreenPageState extends State<MainScreenPage> {
  int _currentTab = 0;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    Color resolveIconColor(int index) =>
        _currentTab == index
            ? theme.bottomNavigationBarTheme.selectedItemColor!
            : theme.bottomNavigationBarTheme.unselectedItemColor!;
    final strings = S.of(context);
    return Scaffold(
      bottomNavigationBar: BottomNavigationBar(
        type: BottomNavigationBarType.fixed,
        currentIndex: _currentTab,
        elevation: _currentTab == 2 ? 20 : 0,
        showSelectedLabels: false,
        showUnselectedLabels: false,
        items: [
          BottomNavigationBarItem(
            icon: Assets.images.icons.icList.svg(color: resolveIconColor(0)),
            label: strings.tabCounters,
          ),
          BottomNavigationBarItem(
            icon: Assets.images.icons.icDie.svg(color: resolveIconColor(1)),
            label: strings.tabDice,
          ),
          BottomNavigationBarItem(
            icon: Assets.images.icons.icMore.svg(color: resolveIconColor(2)),
            label: strings.tabSettings,
          ),
        ],
        onTap: (index) {
          setState(() => _currentTab = index);
        },
      ),
      body: Center(
        child: Text('$_currentTab'),
      ),
    );
  }
}
