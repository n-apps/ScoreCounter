/*
 * Copyright (c) 2022 Score Counter
 */

import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:score_counter/feature/more/events.dart';
import 'package:score_counter/feature/more/state.dart';

class MoreBloc extends Bloc<MoreEvent, MoreState> {
  MoreBloc() : super(const MoreState());
}
