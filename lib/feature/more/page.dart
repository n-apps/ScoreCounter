/*
 * Copyright (c) 2022 Score Counter
 */

import 'package:flutter/material.dart';

class MoreWidget extends StatefulWidget {
  const MoreWidget({Key? key}) : super(key: key);

  @override
  State<MoreWidget> createState() => _MoreWidgetState();
}

class _MoreWidgetState extends State<MoreWidget> {
  @override
  Widget build(BuildContext context) {
    return const Center(child: Text('More'));
  }
}
