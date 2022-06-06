/*
 * Copyright (c) 2022 Score Counter
 */

import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:score_counter/feature/dice/events.dart';
import 'package:score_counter/feature/dice/state.dart';

class DiceBloc extends Bloc<DiceEvent, DiceState> {
  DiceBloc() : super(const DiceState());
}
