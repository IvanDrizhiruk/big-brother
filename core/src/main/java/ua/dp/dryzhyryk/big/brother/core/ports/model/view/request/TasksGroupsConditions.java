package ua.dp.dryzhyryk.big.brother.core.ports.model.view.request;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TasksGroupsConditions {

	TasksGroupConditions unFunctionalTasksGroupConditions;
	TasksGroupConditions inProgressTasksGroupConditions;
}
