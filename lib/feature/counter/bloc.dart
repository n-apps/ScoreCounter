/*
 * Copyright (c) 2022 Score Counter
 */

import 'dart:math';

import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:score_counter/data/dto.dart';
import 'package:score_counter/data/service/counters.dart';
import 'package:score_counter/feature/counter/events.dart';
import 'package:score_counter/feature/counter/state.dart';
import 'package:score_counter/utils/subscriptions_manager_mixin.dart';
import 'package:score_counter/widget/action_handler.dart';

class CountersBloc extends Bloc<CountersEvent, CountersState>
    with SubscriptionsManagerMixin, ActionAware {
  static const String actionOnAddedCounter = "on_added_counter";
  final CountersService _countersService = CountersService.get();

  CountersBloc() : super(const CountersState()) {
    on<UpdateCountersEvent>(
      (event, emit) => emit(state.copyWith(counters: event.counters)),
    );
    on<AddCounterEvent>(
      (event, emit) => _addNewCounter(),
    );
    on<DeleteCounterEvent>(
      (event, emit) => _countersService.delete(event.counter),
    );
    on<ChangeScoreCounterEvent>(
      (event, emit) => _countersService.update(
          event.counter.copyWith(score: event.counter.score + event.scoreDiff)),
    );
    on<DeleteAllCountersEvent>(
      (event, emit) => _countersService.clear(),
    );
    on<ResetAllCountersEvent>(
      (event, emit) => state.counters
          .map((c) => c.copyWith(score: 0))
          .forEach(_countersService.update),
    );
    autoClose(_countersService.counters().listen((counters) {
      counters.sort((a, b) => a.position.compareTo(b.position));
      add(UpdateCountersEvent(counters));
      _detectWinner(counters);
    }));
  }

  Future<void> _addNewCounter() async {
    await _countersService.add(CounterDto(
      name: 'Player ${Random().nextInt(1000)}',
      color: 0xff000000,
      score: 0,
      position: state.counters.length,
    ));
    sendAction(actionOnAddedCounter);
  }

  void _detectWinner(List<CounterDto> counters) {}
}
