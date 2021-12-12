package ua.dp.dryzhyryk.big.brother.core;

import lombok.extern.slf4j.Slf4j;
import ua.dp.dryzhyryk.big.brother.core.calculator.task.metrics.TasksMetricsForPersonCalculator;
import ua.dp.dryzhyryk.big.brother.core.data.source.JiraInformationHolder;
import ua.dp.dryzhyryk.big.brother.core.calculator.task.work.log.TasksWorkingLogsForPersonsCalculator;
import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.data.Task;
import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.search.conditions.types.JiraPersonSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.PeopleView;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.task.metrics.TasksMetricsForPerson;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.task.working.log.TasksWorkingLogsForPerson;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.request.PeopleSearchConditions;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class BigJiraBrotherPeopleViewProvider {

    private final JiraInformationHolder jiraInformationHolder;
    private final TasksWorkingLogsForPersonsCalculator tasksWorkingLogsForPersonsCalculator;
    private final TasksMetricsForPersonCalculator tasksMetricsForPersonCalculator;

    public BigJiraBrotherPeopleViewProvider(
            JiraInformationHolder jiraInformationHolder,
            TasksWorkingLogsForPersonsCalculator tasksWorkingLogsForPersonsCalculator,
            TasksMetricsForPersonCalculator tasksMetricsForPersonCalculator) {
        this.jiraInformationHolder = jiraInformationHolder;
        this.tasksWorkingLogsForPersonsCalculator = tasksWorkingLogsForPersonsCalculator;
        this.tasksMetricsForPersonCalculator = tasksMetricsForPersonCalculator;
    }


    public PeopleView preparePeopleView(PeopleSearchConditions peopleSearchConditions) {

        List<Task> tasks = peopleSearchConditions.getPeopleNames().stream()
                .map(personName -> JiraPersonSearchConditions.builder()
                        .personName(personName)
                        .startPeriod(peopleSearchConditions.getStartPeriod())
                        .endPeriod(peopleSearchConditions.getEndPeriod())
                        .build())
                .map(jiraInformationHolder::getTasks)
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());

        List<TasksWorkingLogsForPerson> personMetrics = tasksWorkingLogsForPersonsCalculator.calculateTasksWorkingLogsForPersons(tasks, peopleSearchConditions);

        List<TasksMetricsForPerson> tasksMetricsForPersons = tasksMetricsForPersonCalculator.calculateTasksMetricsForPerson(tasks, peopleSearchConditions);

        return PeopleView.builder()
                .teamName(peopleSearchConditions.getTeamName())
                .startPeriod(peopleSearchConditions.getStartPeriod())
                .endPeriod(peopleSearchConditions.getEndPeriod())
                .tasksWorkingLogsForPersons(personMetrics)
                .tasksMetricsForPersons(tasksMetricsForPersons)
                .build();
    }
}
