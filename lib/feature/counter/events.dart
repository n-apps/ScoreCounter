/*
 * Copyright (c) 2022 Score Counter
 */
import 'package:score_counter/data/dto.dart';

abstract class CountersEvent {}

class UpdateCountersEvent extends CountersEvent {
  final List<CounterDto> counters;

  UpdateCountersEvent(this.counters);
}

class AddCounterEvent extends CountersEvent {
  AddCounterEvent();
}

class DeleteCounterEvent extends CountersEvent {
  final CounterDto counter;

  DeleteCounterEvent(this.counter);
}

class ChangeScoreCounterEvent extends CountersEvent {
  /// Can be positive or negative.
  final int scoreDiff;
  final CounterDto counter;

  ChangeScoreCounterEvent(this.scoreDiff, this.counter);
}

class ResetAllCountersEvent extends CountersEvent {}

class DeleteAllCountersEvent extends CountersEvent {}
