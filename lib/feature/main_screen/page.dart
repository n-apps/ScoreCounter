/*
 * Copyright (c) 2022 Score Counter
 */

import 'package:flutter/cupertino.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import 'package:score_counter/feature/main_screen/bloc.dart';

enum NavigationTab {
  counters,
  dice,
  more,
}

class MainScreenPage extends StatelessWidget {
  static Widget buildRoute(BuildContext _, GoRouterState __) =>
      BlocProvider<MainScreenBloc>(
        create: (context) => MainScreenBloc(),
        child: const MainScreenPage(),
      );

  const MainScreenPage({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    // final bloc = context.read<MainScreenBloc>();
    // final theme = CupertinoTheme.of(context);
    // final tabColor = theme.brand.colorNotGray;
    // final tabColorActive = theme.brand.colorNotBlackBlack;
    // final icons = Assets.images.icons;
    return CupertinoTabScaffold(
      tabBar: CupertinoTabBar(
        items: [
          // BottomNavigationBarItem(
          //   icon: icons.icDashboard.svg(color: tabColor, width: 24, height: 24),
          //   activeIcon: icons.icDashboard.svg(color: tabColorActive),
          // ),
          // BottomNavigationBarItem(
          //   icon: icons.icMyFood.svg(color: tabColor),
          //   activeIcon: icons.icMyFood.svg(color: tabColorActive),
          // ),
          // BottomNavigationBarItem(
          //   icon: icons.icChat.svg(color: tabColor),
          //   activeIcon: icons.icChat.svg(color: tabColorActive),
          // ),
        ],
      ),
      tabBuilder: (context, index) {
        switch (index) {
          case 0:
            // return DashboardPage(bloc: bloc.dashboardBloc);
          default:
            return Center(
              child: Text('Tab $index'),
            );
        }
      },
    );
  }
}
