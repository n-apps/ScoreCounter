/*
 * Copyright (c) 2022 Score Counter
 * 2020-2021 NaikSoftware, drstranges, MagTuxGit
 */

import 'package:resource_repository/resource_repository.dart';

CacheStorage<K, V> createObjectBoxCacheStorage<K, V>(
  //ignore: avoid-unused-parameters
  String boxKey, {
  //ignore: avoid-unused-parameters
  required V Function(dynamic json) decode,
}) {
  throw UnimplementedError('ObjectBox can be used on target platform');
}
