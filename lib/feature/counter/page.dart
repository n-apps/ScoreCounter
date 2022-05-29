/*
 * Copyright (c) 2022 Score Counter
 */

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:score_counter/feature/counter/bloc.dart';

class CountersWidget extends StatefulWidget {

  const CountersWidget({Key? key}) : super(key: key);

  @override
  State<CountersWidget> createState() => _CountersWidgetState();
}

class _CountersWidgetState extends State<CountersWidget> {
  @override
  Widget build(BuildContext context) {
    return BlocBuilder<CountersBloc, CountersState>(
      builder: (context, state) => const Center(child: Text('Counters')),
    );
  }
}
