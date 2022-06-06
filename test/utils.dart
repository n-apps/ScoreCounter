/*
 * Copyright (c) 2022 Score Counter
 */
import 'package:score_counter/dependencies.dart';

void registerSingleton<T extends Object>(T instance) =>
    getIt.registerSingleton<T>(instance);
