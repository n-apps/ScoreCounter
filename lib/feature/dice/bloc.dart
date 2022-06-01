/*
 * Copyright (c) 2022 Score Counter
 */

import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class DiceBloc extends Bloc<DiceEvent, DiceState> {
  DiceBloc() : super(const DiceState());
}

class DiceState extends Equatable {
  const DiceState();

  @override
  List<Object?> get props => [];
}

abstract class DiceEvent {}
