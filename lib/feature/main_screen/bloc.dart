/*
 * Copyright (c) 2022 Score Counter
 */

import 'package:bloc/bloc.dart';
import 'package:score_counter/feature/main_screen/state.dart';

class MainScreenBloc extends Bloc<MainScreenEvent, MainScreenState> {
  MainScreenBloc()
      : super(const MainScreenState(
          currentTab: NavigationTab.counters,
        )) {
    on<ChangeNavigationTabEvent>(
      (event, emit) => emit(state.copyWith(currentTab: event.tab)),
    );
  }
}

abstract class MainScreenEvent {}

class ChangeNavigationTabEvent extends MainScreenEvent {
  final NavigationTab tab;

  ChangeNavigationTabEvent(this.tab);
}
