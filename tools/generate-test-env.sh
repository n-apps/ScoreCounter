#!/bin/bash
#
# Copyright (c) 2022 Score Counter
#

echo "import 'package:firebase_core/firebase_core.dart' show FirebaseOptions;

class DefaultFirebaseOptions {
  static FirebaseOptions get currentPlatform {
    throw UnsupportedError(
      'DefaultFirebaseOptions have not been configured for tests',
    );
  }
}" > lib/firebase_options.dart
