#!/bin/bash
#
# Copyright (c) 2022 Score Counter
#

echo "import 'package:firebase_core/firebase_core.dart' show FirebaseOptions;

class DefaultFirebaseOptions {
  static FirebaseOptions get currentPlatform {
    throw UnsupportedError(
      'DefaultFirebaseOptions have not been configured for macos - '
          'you can reconfigure this by running the FlutterFire CLI again.',
    );
  }
}" > lib/firebase_options.dart
