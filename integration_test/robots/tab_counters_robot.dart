/*
 * Copyright (c) 2022 Score Counter
 */
import 'package:flutter/cupertino.dart';
import 'package:flutter_test/flutter_test.dart';

class TabCountersRobot {
  final WidgetTester tester;

  TabCountersRobot(this.tester);

  Future<void> clickAddCounter() async {
    await tester.tap(find.byIcon(CupertinoIcons.add));
    await tester.pumpAndSettle();
  }

  int getExpandedCountersCount() =>
      (find.byKey(const ObjectKey('expanded_counters')).evaluate().first.widget
              as Column)
          .children
          .length;
}
