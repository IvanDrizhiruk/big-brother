package ua.dp.dryzhyryk.big.brother.core;

import lombok.extern.slf4j.Slf4j;
import ua.dp.dryzhyryk.big.brother.core.data.source.JiraInformationHolder;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.person.TasksWorkingLogsForPersonsCalculator;
import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.data.Task;
import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.search.conditions.types.JiraPersonSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.PeopleView;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.task.working.log.TasksWorkingLogsForPerson;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.request.PeopleSearchConditions;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class BigJiraBrotherPeopleViewProvider {

    private final JiraInformationHolder jiraInformationHolder;
    private final TasksWorkingLogsForPersonsCalculator peopleViewMetricsCalculator;

    public BigJiraBrotherPeopleViewProvider(
            JiraInformationHolder jiraInformationHolder,
            TasksWorkingLogsForPersonsCalculator peopleViewMetricsCalculator) {
        this.jiraInformationHolder = jiraInformationHolder;
        this.peopleViewMetricsCalculator = peopleViewMetricsCalculator;
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

        List<TasksWorkingLogsForPerson> personMetrics = peopleViewMetricsCalculator.calculateTasksWorkingLogsForPersons(tasks, peopleSearchConditions);

        //TODO add calculation

        return PeopleView.builder()
                .teamName(peopleSearchConditions.getTeamName())
                .startPeriod(peopleSearchConditions.getStartPeriod())
                .endPeriod(peopleSearchConditions.getEndPeriod())
                .tasksWorkingLogsForPersons(personMetrics)
                .build();
    }
}
