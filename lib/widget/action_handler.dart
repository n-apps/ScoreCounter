/*
 * Copyright (c) 2022 Score Counter
 */

import 'dart:async';

import 'package:equatable/equatable.dart';
import 'package:flutter/widgets.dart';
import 'package:rxdart/rxdart.dart';

/// Adds actions functionality
mixin ActionAware {
  final Subject<ActionEvent> _actions = BehaviorSubject<ActionEvent>();

  /// Listen to the events of this stream
  Stream<ActionEvent> get actions => _actions.where((event) => !event.handled).doOnData((event) => event.consume());

  /// Send action to subscribers
  void sendAction(String action, [dynamic payload]) {
    if (_actions.isClosed) return;
    _actions.add(ActionEvent(action, payload: payload));
  }

  /// Send predefined [ActionEvent.actionShowError]
  void sendActionError([dynamic payload]) => sendAction(ActionEvent.actionShowError, payload);

  /// Dispose resources
  @mustCallSuper
  Future<void> close() async {
    await _actions.close();
  }
}

/// Action event
//ignore: must_be_immutable
class ActionEvent extends Equatable {
  /// Predefined common action
  static const actionShowError = 'error';

  /// Action name
  final String action;

  /// Optional payload
  final dynamic payload;

  /// Whether or not the action was consumed
  bool handled = false;

  /// Create action
  ActionEvent(this.action, {this.payload});

  /// Check action
  bool actionIdentical(String action) => identical(this.action, action);

  /// Mark as handled
  void consume() => handled = true;

  /// Helper for common check on error
  bool get isShowErrorAction => identical(action, actionShowError);

  @override
  List<Object> get props => [action, payload];

  @override
  bool? get stringify => true;
}

/// The widget for handling sent actions
class ActionHandler extends StatefulWidget {
  /// Optional child widget
  final Widget? child;

  /// Actions handler
  final void Function(BuildContext context, ActionEvent event) handler;

  /// Stream to listen
  final Stream<ActionEvent> actions;

  /// Create handler
  const ActionHandler({
    required this.handler,
    required this.actions,
    this.child,
    Key? key,
  }) : super(key: key);

  @override
  State createState() => _ActionHandlerState();
}

class _ActionHandlerState extends State<ActionHandler> {
  StreamSubscription<dynamic>? _subscription;

  @override
  Widget build(BuildContext context) => widget.child ?? const SizedBox.shrink();

  @override
  void initState() {
    super.initState();
    _subscribe();
  }

  @override
  void didUpdateWidget(ActionHandler oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.actions != widget.actions) {
      _unsubscribe();
      _subscribe();
    }
  }

  @override
  void dispose() {
    _unsubscribe();
    super.dispose();
  }

  void _subscribe() {
    _subscription = widget.actions.listen((ActionEvent event) {
      widget.handler(context, event);
    });
  }

  void _unsubscribe() {
    _subscription?.cancel();
  }
}
