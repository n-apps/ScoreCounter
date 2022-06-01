/*
 * Copyright (c) 2022 Score Counter
 */
import 'dart:async';

import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:rxdart/rxdart.dart';

mixin SubscriptionsManagerMixin on Closable {
  final _compositeSubscription = CompositeSubscription();

  void autoClose(StreamSubscription subscription) =>
      _compositeSubscription.add(subscription);
  
  @override
  Future<void> close() async {
    await _compositeSubscription.dispose();
    await super.close();
  }
}
