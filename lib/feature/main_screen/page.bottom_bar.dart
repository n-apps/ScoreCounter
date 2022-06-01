/*
 * Copyright (c) 2022 Score Counter
 */
part of 'page.dart';

class _AppNavigationBar extends StatelessWidget {
  final NavigationTab currentTab;
  final int diceValue;

  const _AppNavigationBar({
    required this.currentTab,
    required this.diceValue,
    super.key,
  });

  @override
  Widget build(BuildContext context) {
    final strings = S.of(context);
    final theme = Theme.of(context);
    Color resolveIconColor(int index) => currentTab.position == index
        ? theme.bottomNavigationBarTheme.selectedItemColor!
        : theme.bottomNavigationBarTheme.unselectedItemColor!;
    return BottomNavigationBar(
      type: BottomNavigationBarType.fixed,
      currentIndex: currentTab.position,
      elevation: currentTab == NavigationTab.more ? 20 : 0,
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
      onTap: (index) => context.read<MainScreenBloc>().add(
            ChangeNavigationTabEvent(NavigationTab.byPosition(index)),
          ),
    );
  }
}
