/*
 * Copyright (c) 2022 Score Counter
 */

import 'package:flutter_bloc/flutter_bloc.dart';

class MoreBloc extends Bloc<MoreEvent, MoreState> {
  MoreBloc() : super(const MoreState());
}

class MoreState {
  const MoreState();
}

abstract class MoreEvent {}
