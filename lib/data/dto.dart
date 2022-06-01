/*
 * Copyright (c) 2022 Score Counter
 */
import 'package:json_annotation/json_annotation.dart';

part 'dto.g.dart';

@JsonSerializable()
class CounterDto {
  final String name;
  final int score;
  final int color;
  final int position;

  CounterDto({
    required this.name,
    required this.score,
    required this.color,
    required this.position,
  });

  CounterDto copyWith({
    String? name,
    int? score,
    int? color,
    int? position,
  }) =>
      CounterDto(
        name: name ?? this.name,
        score: score ?? this.score,
        color: color ?? this.color,
        position: position ?? this.position,
      );

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is CounterDto &&
          runtimeType == other.runtimeType &&
          name == other.name &&
          score == other.score &&
          color == other.color;

  @override
  int get hashCode => name.hashCode ^ score.hashCode ^ color.hashCode;

  factory CounterDto.fromJson(Map<String, dynamic> json) =>
      _$CounterDtoFromJson(json);

  Map<String, dynamic> toJson() => _$CounterDtoToJson(this);
}
