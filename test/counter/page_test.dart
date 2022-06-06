// This is a basic Flutter widget test.
//
// To perform an interaction with a widget in your test, use the WidgetTester
// utility that Flutter provides. For example, you can send tap and scroll
// gestures. You can also use WidgetTester to find child widgets in the widget
// tree, read text, and verify that the values of widget properties are correct.

import 'package:flutter_test/flutter_test.dart';
import 'package:score_counter/data/service/counters.dart';

import '../utils.dart';

class TestSingleCountersService extends CountersService {
  @override
  Future<Set<String>> getNames() => Future.value({'test_name'});
}

void main() {
  testWidgets('The name can be used after deleting the counter with this name',
      (tester) async {
    registerSingleton<CountersService>(TestSingleCountersService());
    // TODO: Write bloc test.
  });
}
