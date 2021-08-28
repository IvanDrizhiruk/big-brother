package ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response;

import lombok.Builder;
import lombok.Value;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.task.working.log.TasksWorkingLogsForPerson;

import java.time.LocalDate;
import java.util.List;

@Value
@Builder
public class PeopleView {

    private final String teamName;
    private final LocalDate startPeriod;
    private final LocalDate endPeriod;

    private final List<TasksWorkingLogsForPerson> tasksWorkingLogsForPersons;
}
