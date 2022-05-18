/*
 * Copyright (c) 2022 Score Counter
 */

import 'package:bloc/bloc.dart';

class MainScreenBloc extends Bloc<MainScreenEvent, MainScreenState> {
  // final DashboardBloc dashboardBloc;

  MainScreenBloc() : super(MainScreenState());

  @override
  Future<void> close() async {
    // dashboardBloc.dispose();
    await super.close();
  }
}

class MainScreenState {}

abstract class MainScreenEvent {}
