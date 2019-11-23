package ua.dp.dryzhyryk.big.brother.core.metrics.calculator;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import lombok.Builder;
import lombok.Value;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.Task;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.TaskTimeMetrics;

public class SprintViewMetricsCalculator {

	public TaskTimeMetrics calculateFor(List<Task> rootTasks) {
		return rootTasks.stream()
				.flatMap(task -> Stream.of(Collections.singletonList(task), task.getSubTasks())
						.flatMap(List::stream))
				.map(task -> TaskDataAggregatop.builder()
						.originalEstimateMinutes(Optional.ofNullable(task.getOriginalEstimateMinutes()).orElse(0))
						.timeSpentMinutes(Optional.ofNullable(task.getTimeSpentMinutes()).orElse(0))
						.build())
				.reduce((x, y) -> TaskDataAggregatop.builder()
						.originalEstimateMinutes(x.getOriginalEstimateMinutes() + y.getOriginalEstimateMinutes())
						.timeSpentMinutes(x.getTimeSpentMinutes() + y.getTimeSpentMinutes())
						.build())
				.map(data -> {

					int originalEstimateMinutes = data.getOriginalEstimateMinutes();
					int timeSpentMinutes = data.getTimeSpentMinutes();

					float timeCoefficient =
							0 == timeSpentMinutes
									? 0
									: ((float) originalEstimateMinutes) / timeSpentMinutes;

					return TaskTimeMetrics.builder()
							.originalEstimateMinutes(originalEstimateMinutes)
							.timeSpentMinutes(timeSpentMinutes)
							.timeCoefficient(timeCoefficient)
							.build();
				})
				.orElseGet(() -> TaskTimeMetrics.builder().build());
	}

	@Value
	@Builder
	private static class TaskDataAggregatop {
		private final int originalEstimateMinutes;
		private final int timeSpentMinutes;
	}
}
