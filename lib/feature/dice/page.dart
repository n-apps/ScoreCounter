/*
 * Copyright (c) 2022 Score Counter
 */

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:score_counter/feature/dice/bloc.dart';
import 'package:score_counter/feature/dice/state.dart';

class DiceWidget extends StatefulWidget {
  const DiceWidget({super.key});

  @override
  State<DiceWidget> createState() => _DiceWidgetState();
}

class _DiceWidgetState extends State<DiceWidget> {
  @override
  Widget build(BuildContext context) {
    return BlocBuilder<DiceBloc, DiceState>(builder: (context, state) {
      return const Center(child: Text('Dice'));
    });
  }
}
