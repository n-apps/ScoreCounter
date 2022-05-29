/*
 * Copyright (c) 2022 Score Counter
 */

import 'package:bloc/bloc.dart';

class MainScreenBloc extends Bloc<MainScreenEvent, MainScreenState> {
  MainScreenBloc() : super(const MainScreenState());
}

class MainScreenState {
  const MainScreenState();
}

abstract class MainScreenEvent {}
