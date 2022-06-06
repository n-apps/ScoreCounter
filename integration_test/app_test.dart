/*
 * Copyright (c) 2022 Score Counter
 */
import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';
import 'package:score_counter/bootstrap.dart';
import 'package:score_counter/data/service/counters.dart';
import 'package:score_counter/main.dart' as app;

import 'robots/app_robot.dart';
import 'robots/tab_counters_robot.dart';

void main() {
  final binding = IntegrationTestWidgetsFlutterBinding.ensureInitialized();
  isIntegrationTest = true;

  group('End to end tests', () {
    testWidgets('Add button should add counter when clicked', (tester) async {
      app.main();
      await tester.pumpAndSettle();
      // Can be used to take screenshots.
      final appRobot = AppRobot(binding, tester);

      await CountersService.get().clear();
      await tester.pumpAndSettle();

      final tabCountersRobot = TabCountersRobot(tester);
      await tabCountersRobot.clickAddCounter();

      expect(tabCountersRobot.getExpandedCountersCount(), 1);
    });
  });
}
