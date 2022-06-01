// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CounterDto _$CounterDtoFromJson(Map<String, dynamic> json) => CounterDto(
      name: json['name'] as String,
      score: json['score'] as int,
      color: json['color'] as int,
      position: json['position'] as int,
    );

Map<String, dynamic> _$CounterDtoToJson(CounterDto instance) =>
    <String, dynamic>{
      'name': instance.name,
      'score': instance.score,
      'color': instance.color,
      'position': instance.position,
    };
