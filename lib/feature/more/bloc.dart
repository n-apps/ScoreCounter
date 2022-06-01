/*
 * Copyright (c) 2022 Score Counter
 */

import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class MoreBloc extends Bloc<MoreEvent, MoreState> {
  MoreBloc() : super(const MoreState());
}

class MoreState extends Equatable {
  const MoreState();

  @override
  List<Object?> get props => [];
}

abstract class MoreEvent {}
