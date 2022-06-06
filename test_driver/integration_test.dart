/*
 * Copyright (c) 2022 Score Counter
 */
import 'dart:io';

import 'package:integration_test/integration_test_driver_extended.dart';

Future<void> main() => integrationDriver(
      onScreenshot: (String screenshotName, List<int> screenshotBytes) async {
        final image = await File('screenshots/$screenshotName.png')
            .create(recursive: true);
        image.writeAsBytesSync(screenshotBytes);
        return true;
      },
    );
