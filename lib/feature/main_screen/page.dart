/*
 * Copyright (c) 2022 Score Counter
 */

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import 'package:score_counter/feature/counter/bloc.dart';
import 'package:score_counter/feature/counter/page.dart';
import 'package:score_counter/feature/dice/bloc.dart';
import 'package:score_counter/feature/dice/page.dart';
import 'package:score_counter/feature/main_screen/bloc.dart';
import 'package:score_counter/feature/main_screen/events.dart';
import 'package:score_counter/feature/main_screen/state.dart';
import 'package:score_counter/feature/more/bloc.dart';
import 'package:score_counter/feature/more/page.dart';
import 'package:score_counter/generated/assets.gen.dart';
import 'package:score_counter/generated/l10n.dart';

part 'page.bottom_bar.dart';

class MainScreenPage extends StatelessWidget {
  const MainScreenPage({super.key});

  static Widget buildRoute(BuildContext _, GoRouterState __) =>
      MultiBlocProvider(
        providers: [
          BlocProvider(create: (_) => MainScreenBloc()),
          BlocProvider(create: (_) => CountersBloc()),
          BlocProvider(create: (_) => DiceBloc()),
          BlocProvider(create: (_) => MoreBloc()),
        ],
        child: const MainScreenPage(),
      );

  @override
  Widget build(BuildContext context) => AnnotatedRegion<SystemUiOverlayStyle>(
        // ignore: avoid-non-null-assertion, appBarTheme defined in app theme
        value: Theme.of(context).appBarTheme.systemOverlayStyle!,
        child: BlocBuilder<MainScreenBloc, MainScreenState>(
          builder: (context, state) => Scaffold(
            bottomNavigationBar: _AppNavigationBar(
              currentTab: state.currentTab,
              diceValue: 0,
            ),
            body: WillPopScope(
              onWillPop: () => _onWillPop(context, state),
              child: SafeArea(
                child: const {
                  NavigationTab.counters: CountersWidget(),
                  NavigationTab.dice: DiceWidget(),
                  NavigationTab.more: MoreWidget(),
                }[state.currentTab]!,
              ),
            ),
          ),
        ),
      );

  Future<bool> _onWillPop(BuildContext context, MainScreenState state) {
    if (state.currentTab != NavigationTab.counters) {
      context.read<MainScreenBloc>().add(
            ChangeNavigationTabEvent(NavigationTab.counters),
          );
      return Future.value(false);
    } else {
      return Future.value(true);
    }
  }
}
