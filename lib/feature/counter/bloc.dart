/*
 * Copyright (c) 2022 Score Counter
 */

import 'package:flutter_bloc/flutter_bloc.dart';

class CountersBloc extends Bloc<CountersEvent, CountersState> {
  CountersBloc() : super(const CountersState());
}

class CountersState {
  const CountersState();
}

abstract class CountersEvent {}
