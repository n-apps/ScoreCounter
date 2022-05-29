/*
 * Copyright (c) 2022 Score Counter
 */

import 'package:flutter_bloc/flutter_bloc.dart';

class DiceBloc extends Bloc<DiceEvent, DiceState> {
  DiceBloc() : super(const DiceState());
}

class DiceState {
  const DiceState();
}

abstract class DiceEvent {}
