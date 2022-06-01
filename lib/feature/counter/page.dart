/*
 * Copyright (c) 2022 Score Counter
 */

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:score_counter/data/dto.dart';
import 'package:score_counter/feature/counter/bloc.dart';
import 'package:score_counter/feature/counter/events.dart';
import 'package:score_counter/feature/counter/state.dart';
import 'package:score_counter/generated/l10n.dart';
import 'package:score_counter/widget/action_handler.dart';

part 'page.counters.dart';

class CountersWidget extends StatefulWidget {
  const CountersWidget({super.key});

  @override
  State<CountersWidget> createState() => _CountersWidgetState();
}

class _CountersWidgetState extends State<CountersWidget> {
  final _scrollController = ScrollController();

  @override
  void dispose() {
    _scrollController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => ActionHandler(
        actions: context.watch<CountersBloc>().actions,
        handler: (context, action) {
          if (action.actionIdentical(CountersBloc.actionOnAddedCounter)) {
            _handleCounterAdded(context);
          }
        },
        child: BlocBuilder<CountersBloc, CountersState>(
          builder: (context, state) => Column(
            children: [
              _CountersToolbar(winners: state.winners),
              Expanded(
                  child: state.counters.length > 5
                      ? _CountersScrollableList(
                          counters: state.counters,
                          scrollController: _scrollController,
                        )
                      : _CountersExpandedList(counters: state.counters)),
            ],
          ),
        ),
      );

  void _handleCounterAdded(BuildContext context) {
    Future.delayed(const Duration(milliseconds: 240)).then((_) {
      if (!mounted || _scrollController.position.maxScrollExtent == 0) return;
      _scrollController.animateTo(
        _scrollController.position.maxScrollExtent,
        duration: kThemeAnimationDuration,
        curve: Curves.easeOut,
      );
    });
    final strings = S.of(context);
    ScaffoldMessenger.of(context)
      ..clearSnackBars()
      ..showSnackBar(SnackBar(
        content: Text(strings.counterAdded),
        action: SnackBarAction(
            label: strings.snackbarActionOneMore,
            onPressed: () =>
                context.read<CountersBloc>().add(AddCounterEvent())),
      ));
  }
}

class _CountersToolbar extends StatelessWidget {
  final List<CounterDto> winners;

  const _CountersToolbar({
    required this.winners,
    super.key,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Row(children: [
      _WinnerState(winners: winners),
      const Spacer(),
      IconButton(
        onPressed: () => context.read<CountersBloc>().add(AddCounterEvent()),
        icon: const Icon(CupertinoIcons.add),
        color: theme.colorScheme.onSurface,
      ),
    ]);
  }
}

class _WinnerState extends StatelessWidget {
  final List<CounterDto> winners;

  const _WinnerState({
    required this.winners,
    super.key,
  });

  @override
  Widget build(BuildContext context) {
    return Container();
  }
}
