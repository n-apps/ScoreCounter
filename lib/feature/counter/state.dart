/*
 * Copyright (c) 2022 Score Counter
 */

import 'package:equatable/equatable.dart';
import 'package:score_counter/data/dto.dart';

class CountersState extends Equatable {
  final List<CounterDto> counters;
  final List<CounterDto> winners;

  @override
  List<Object> get props => [counters, winners];

  const CountersState({
    this.counters = const [],
    this.winners = const [],
  });

  CountersState copyWith({
    List<CounterDto>? counters,
    List<CounterDto>? winners,
  }) =>
      CountersState(
        counters: counters ?? this.counters,
        winners: winners ?? this.winners,
      );
}
