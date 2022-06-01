/*
 * Copyright (c) 2022 Score Counter
 */
import 'package:equatable/equatable.dart';

class MainScreenState extends Equatable {
  final NavigationTab currentTab;

  const MainScreenState({
    required this.currentTab,
  });

  MainScreenState copyWith({
    NavigationTab? currentTab,
  }) =>
      MainScreenState(
        currentTab: currentTab ?? this.currentTab,
      );

  @override
  List<Object?> get props => [currentTab];
}

enum NavigationTab {
  counters(0),
  dice(1),
  more(2);

  final int position;

  const NavigationTab(this.position);

  static NavigationTab byPosition(int position) =>
      values.firstWhere((tab) => tab.position == position);
}