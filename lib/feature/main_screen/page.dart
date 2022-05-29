/*
 * Copyright (c) 2022 Score Counter
 */

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import 'package:score_counter/feature/counter/bloc.dart';
import 'package:score_counter/feature/counter/page.dart';
import 'package:score_counter/feature/dice/bloc.dart';
import 'package:score_counter/feature/dice/page.dart';
import 'package:score_counter/feature/main_screen/bloc.dart';
import 'package:score_counter/feature/more/bloc.dart';
import 'package:score_counter/feature/more/page.dart';
import 'package:score_counter/generated/assets.gen.dart';
import 'package:score_counter/generated/l10n.dart';

enum NavigationTab {
  counters(0, CountersWidget()),
  dice(1, DiceWidget()),
  more(2, MoreWidget());

  final int position;
  final Widget widget;

  const NavigationTab(this.position, this.widget);

  static NavigationTab byPosition(int position) =>
      values.firstWhere((tab) => tab.position == position);
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
  NavigationTab _currentTab = NavigationTab.counters;
  final Map<NavigationTab, Widget> _tabsCache = {};

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    Color resolveIconColor(int index) => _currentTab.position == index
        ? theme.bottomNavigationBarTheme.selectedItemColor!
        : theme.bottomNavigationBarTheme.unselectedItemColor!;
    final strings = S.of(context);
    return Scaffold(
      bottomNavigationBar: BottomNavigationBar(
        type: BottomNavigationBarType.fixed,
        currentIndex: _currentTab.position,
        elevation: _currentTab == NavigationTab.more ? 20 : 0,
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
          setState(() => _currentTab = NavigationTab.byPosition(index));
        },
      ),
      body: MultiBlocProvider(
        providers: [
          BlocProvider(create: (_) => CountersBloc()),
          BlocProvider(create: (_) => DiceBloc()),
          BlocProvider(create: (_) => MoreBloc()),
        ],
        child: _currentTab.widget,
      ),
    );
  }
}
