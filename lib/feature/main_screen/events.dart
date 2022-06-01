/*
 * Copyright (c) 2022 Score Counter
 */
import 'package:score_counter/feature/main_screen/state.dart';

abstract class MainScreenEvent {}

class ChangeNavigationTabEvent extends MainScreenEvent {
  final NavigationTab tab;

  ChangeNavigationTabEvent(this.tab);
}