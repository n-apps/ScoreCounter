/*
 * Copyright (c) 2022 Score Counter
 * 2020-2021 NaikSoftware, drstranges, MagTuxGit
 */

import 'package:flutter/foundation.dart';

class Resource<T> {
  final ResourceState state;
  final T? data;
  final String? message;
  final dynamic error;

  bool get isLoading => state == ResourceState.loading;

  bool get isNotLoading => state != ResourceState.loading;

  bool get isError => state == ResourceState.error;

  bool get isNotError => state != ResourceState.error;

  bool get isSuccess => state == ResourceState.success;

  bool get hasData => data != null;

  Resource._(this.state, {this.data, this.message, this.error});

  factory Resource.loading([T? data]) =>
      Resource._(ResourceState.loading, data: data);

  factory Resource.success([T? data]) =>
      Resource._(ResourceState.success, data: data);

  factory Resource.error(String message, {dynamic error, T? data}) =>
      Resource._(ResourceState.error,
          message: message, error: error, data: data);

  Resource<NewType> map<NewType>(NewType? Function(T?) transform) =>
      Resource._(state, data: transform(data), message: message, error: error);

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is Resource &&
          runtimeType == other.runtimeType &&
          state == other.state &&
          _compareData(data, other.data) &&
          message == other.message &&
          error == other.error;

  bool _compareData(dynamic v1, dynamic v2) =>
      v1 is List && v2 is List ? listEquals(v1, v2) : v1 == v2;

  @override
  int get hashCode =>
      state.hashCode ^ data.hashCode ^ message.hashCode ^ error.hashCode;

  @override
  String toString() {
    return 'State : $state \n Message : $message \n Data : $data';
  }
}

enum ResourceState {
  loading,
  success,
  error,
}
