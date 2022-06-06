/*
 * Copyright (c) 2022 Score Counter
 */
part of 'page.dart';

class _CountersExpandedList extends StatelessWidget {
  final List<CounterDto> counters;

  const _CountersExpandedList({
    required this.counters,
  });

  @override
  Widget build(BuildContext context) => LayoutBuilder(
        builder: (
          context,
          constraints,
        ) =>
            Column(
          key: const ObjectKey('expanded_counters'),
          children: counters
              .map((c) => _CounterLarge(
                    key: ValueKey(c.name),
                    counter: c,
                    height: constraints.maxHeight / counters.length,
                  ))
              .toList(),
        ),
      );
}

class _CountersScrollableList extends StatelessWidget {
  final List<CounterDto> counters;
  final ScrollController scrollController;

  const _CountersScrollableList({
    required this.counters,
    required this.scrollController,
  });

  @override
  Widget build(BuildContext context) => ListView.separated(
        controller: scrollController,
        separatorBuilder: (_, __) => const SizedBox(height: 8),
        padding: const EdgeInsets.only(bottom: 72),
        itemCount: counters.length,
        itemBuilder: (context, index) {
          final counter = counters[index];
          return _CounterCompact(key: ValueKey(counter.name), counter: counter);
        },
      );
}

class _CounterLarge extends StatelessWidget {
  final CounterDto counter;
  final double height;

  const _CounterLarge({
    required this.counter,
    required this.height,
    super.key,
  });

  @override
  Widget build(BuildContext context) {
    const verticalPadding = 8.0;
    // ignore: avoid-wrapping-in-padding, Ink padding can't be used as container's padding
    return Padding(
      padding: const EdgeInsets.fromLTRB(8, 0, 8, verticalPadding),
      child: Ink(
        decoration: BoxDecoration(
          color: Color(counter.color),
          borderRadius: const BorderRadius.all(Radius.circular(2)),
        ),
        child: InkWell(
          onLongPress: () =>
              context.read<CountersBloc>().add(DeleteCounterEvent(counter)),
          borderRadius: const BorderRadius.all(Radius.circular(2)),
          child: SizedBox(
            height: height - verticalPadding,
            width: double.infinity,
            child: Text(
              counter.name,
              style: const TextStyle(color: Colors.white),
            ),
          ),
        ),
      ),
    );
  }
}

class _CounterCompact extends StatelessWidget {
  final CounterDto counter;

  const _CounterCompact({
    required this.counter,
    super.key,
  });

  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.symmetric(horizontal: 8),
        child: Material(
          child: Ink(
            decoration: BoxDecoration(
              color: Color(counter.color),
              borderRadius: const BorderRadius.all(Radius.circular(2)),
            ),
            child: InkWell(
              onLongPress: () =>
                  context.read<CountersBloc>().add(DeleteCounterEvent(counter)),
              borderRadius: const BorderRadius.all(Radius.circular(2)),
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Text(
                  '${counter.position} -> ${counter.name}',
                  style: const TextStyle(color: Colors.white),
                ),
              ),
            ),
          ),
        ),
      );
}
