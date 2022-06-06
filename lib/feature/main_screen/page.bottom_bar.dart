/*
 * Copyright (c) 2022 Score Counter
 */
part of 'page.dart';

class _AppNavigationBar extends StatelessWidget {
  final NavigationTab currentTab;
  final int diceValue;
  static const double _navigationElevation = 20;

  const _AppNavigationBar({
    required this.currentTab,
    required this.diceValue,
  });

  @override
  Widget build(BuildContext context) {
    final strings = S.of(context);
    final theme = Theme.of(context);
    final barTheme = theme.bottomNavigationBarTheme;

    return BottomNavigationBar(
      type: BottomNavigationBarType.fixed,
      currentIndex: currentTab.position,
      elevation: currentTab == NavigationTab.more ? _navigationElevation : 0,
      items: [
        BottomNavigationBarItem(
          icon: Assets.images.icons.icList.svg(color: _iconColor(0, barTheme)),
          label: strings.tabCounters,
        ),
        BottomNavigationBarItem(
          icon: Assets.images.icons.icDie.svg(color: _iconColor(1, barTheme)),
          label: strings.tabDice,
        ),
        BottomNavigationBarItem(
          icon: Assets.images.icons.icMore.svg(color: _iconColor(2, barTheme)),
          label: strings.tabSettings,
        ),
      ],
      onTap: (index) => context.read<MainScreenBloc>().add(
            ChangeNavigationTabEvent(NavigationTab.byPosition(index)),
          ),
    );
  }

  Color _iconColor(int index, BottomNavigationBarThemeData barTheme) {
    return currentTab.position == index
        // ignore: avoid-non-null-assertion, defined in app theme
        ? barTheme.selectedItemColor!
        // ignore: avoid-non-null-assertion, defined in app theme
        : barTheme.unselectedItemColor!;
  }
}
