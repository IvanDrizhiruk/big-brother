package ua.dp.dryzhyryk.big.brother;

import ua.dp.dryzhyryk.big.brother.core.data.source.model.Task;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.TasksTreeView;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.SprintSearchConditions;

import java.util.Collections;
import java.util.List;

public final class TestDoublesForProjectSprintMega {

    private TestDoublesForProjectSprintMega() {
    }

    public static SprintSearchConditions newSearchConditionsMega() {
        return SprintSearchConditions.builder()
                .project("Mega project")
                .sprint("Mega first sprint")
                .build();
    }

    public static List<Task> newProjectSprintMega() {
        return Collections.emptyList();
    }

    public static List<TasksTreeView> newTaskViewForProjectSprintMega() {
        return null;
    }
}
