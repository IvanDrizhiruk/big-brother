package ua.dp.dryzhyryk.big.brother.tests;

import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.mockito.Mockito;
import ua.dp.dryzhyryk.big.brother.app.ReportGeneratorMock;
import ua.dp.dryzhyryk.big.brother.base.BaseSteps;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.PeopleView;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.person.model.PersonMetrics;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.person.model.TaskWorkingLogMetrics;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.person.model.TimeSpentByDay;
import ua.dp.dryzhyryk.big.brother.core.utils.DateTimeProvider;
import ua.dp.dryzhyryk.big.brother.resources.jira.BigBrotherConsoleApplication;
import ua.dp.dryzhyryk.big.brother.resources.jira.search.PeopleSearchRequest;
import ua.dp.dryzhyryk.big.brother.resources.jira.search.SearchRequests;

import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class ReportByPersonForLastFinishedWeekSteps extends BaseSteps {

    private final BigBrotherConsoleApplication app;
    private final DateTimeProvider dateTimeProviderMock;
    private final ReportGeneratorMock reportGeneratorMock;
    private PeopleView lastGeneratedPeopleView;

    public ReportByPersonForLastFinishedWeekSteps(BigBrotherConsoleApplication app, DateTimeProvider dateTimeProviderMock, ReportGeneratorMock reportGeneratorMock) {
        this.app = app;
        this.dateTimeProviderMock = dateTimeProviderMock;
        this.reportGeneratorMock = reportGeneratorMock;
    }

    @When("prepare report for person $person for last finished week executed at date $dateOfExecution")
    public void whenPrepareReportForPersonForLastFinishedWeekRunAtDate(
            @Named("person") String person,
            @Named("dateOfExecution") String dateOfExecution) {

        Mockito.when(dateTimeProviderMock.nowLocalDate()).thenReturn(LocalDate.parse(dateOfExecution));

        PeopleSearchRequest peopleSearchRequest = PeopleSearchRequest.builder()
                .teamName("Ducks")
                .peopleNames(Collections.singletonList(person))
                .build();

        SearchRequests searchRequests = SearchRequests.builder()
                .peopleSearchConditions(Collections.singletonList(peopleSearchRequest))
                .build();

        app.prepareReportByPersonForLastFinishedWeek(searchRequests);

        this.lastGeneratedPeopleView = reportGeneratorMock.getLastGeneratedPeopleView();
    }

    @Then("PeopleView common fields is: $peopleViewExpectedTable")
    public void thenPeopleViewCommonFieldsIs(
            @Named("peopleViewExpectedTable") ExamplesTable peopleViewExpectedTable) {

        PeopleView expectedPeopleView = peopleViewExpectedTable.getRowsAsParameters().stream()
                .map(parameter -> PeopleView.builder()
                        .teamName(parameter.valueAs("TeamName", String.class))
                        .startPeriod(parameter.valueAs("StartPeriod", LocalDate.class))
                        .endPeriod(parameter.valueAs("EndPeriod", LocalDate.class))
                        .build())
                .findFirst()
                .orElseThrow();

        assertThat(lastGeneratedPeopleView)
                .isEqualToIgnoringGivenFields(expectedPeopleView, "personMetrics");

    }

    @Then("PeopleView PersonMetrics present for persons: $persons")
    public void thenPeopleViewPersonMetricsForPersons(
            @Named("persons") List<String> expectedPersons) {

        List<String> actualPersons = lastGeneratedPeopleView.getPersonMetrics().stream()
                .map(PersonMetrics::getPerson)
                .collect(Collectors.toList());

        assertThat(actualPersons)
                .isEqualTo(expectedPersons);
    }

    @Then("PeopleView PersonMetrics common fields is: $personMetricsExamplesTable")
    public void thenPeopleViewPersonMetricsForPersonCommonFieldsIs(
            @Named("personMetricsExamplesTable") ExamplesTable personMetricsExamplesTable) {

        List<PersonMetrics> expectedPersonMetrics = personMetricsExamplesTable.getRowsAsParameters().stream()
                .map(parameter -> PersonMetrics.builder()
                        .person(parameter.valueAs("Person", String.class))
                        .totalTimeSpentInCurrentPeriodInMinutes(parameter.valueAs("TotalTimeSpentInCurrentPeriodInMinutes", Integer.class))
                        .totalTimeSpentOnTaskInMinutes(parameter.valueAs("TotalTimeSpentOnTaskInMinutes", Integer.class))
                        .build())
                .collect(Collectors.toList());

        assertThat(lastGeneratedPeopleView.getPersonMetrics())
                .usingElementComparatorIgnoringFields("dailyTaskLogs", "totalTimeSpentByDay")
                .isEqualTo(expectedPersonMetrics);
    }

    @Then("PeopleView PersonMetrics DailyTaskLogs for $person is: $dailyTaskLogsExamplesTable")
    public void thenPeopleViewPersonMetricsDailyTaskLogsForUserIs(
            @Named("person") String person,
            @Named("dailyTaskLogsExamplesTable") ExamplesTable dailyTaskLogsExamplesTable) {

        List<TaskWorkingLogMetrics> expectedDailyTaskLogs = dailyTaskLogsExamplesTable.getRowsAsParameters().stream()
                .map(parameter -> TaskWorkingLogMetrics.builder()
                        .taskId(parameter.valueAs("TaskId", String.class))
                        .taskName(parameter.valueAs("TaskName", String.class))
                        .taskExternalStatus(parameter.valueAs("TaskExternalStatus", String.class))
                        .totalTimeSpentByPeriodInMinutes(parameter.valueAs("TotalTimeSpentByPeriodInMinutes", Integer.class))
                        .totalTimeSpentOnTaskInMinutes(parameter.valueAs("TotalTimeSpentOnTaskInMinutes", Integer.class))
                        .timeSpentMinutes(parameter.valueAs("TimeSpentMinutes", Integer.class))
                        .originalEstimateMinutes(parameter.valueAs("OriginalEstimateMinutes", Integer.class))
                        .timeCoefficient(parameter.valueAs("TimeCoefficient", Float.class))
                        .build())
                .collect(Collectors.toList());

        List<TaskWorkingLogMetrics> dailyTaskLogsForPerson = lastGeneratedPeopleView.getPersonMetrics().stream()
                .filter(personMetric -> person.equals(personMetric.getPerson()))
                .findFirst()
                .map(PersonMetrics::getDailyTaskLogs)
                .orElseThrow();

        assertThat(dailyTaskLogsForPerson)
                .usingElementComparatorIgnoringFields("timeSpentByDays")
                .containsExactlyInAnyOrderElementsOf(expectedDailyTaskLogs);
    }

    @Then("PeopleView PersonMetrics DailyTaskLogs TimeSpentByDay in Minutes for $person is: $timeSpentByDayexamplesTable")
    public void thenPeopleViewPersonMetricsDailyTaskLogsTimeSpentByDayInMinutesForUserIs(
            @Named("person") String person,
            @Named("timeSpentByDayexamplesTable") ExamplesTable timeSpentByDayExamplesTable) {

        List<String> headersDates = timeSpentByDayExamplesTable.getHeaders().stream()
                .filter(header -> !"TaskId".equals(header))
                .collect(Collectors.toList());

        Map<String, List<TimeSpentByDay>> expectedTimeSpentByDay = timeSpentByDayExamplesTable.getRowsAsParameters().stream()
                .map(parameter -> {

                    String taskId = parameter.valueAs("TaskId", String.class);

                    List<TimeSpentByDay> timeSpentByDays = headersDates.stream()
                            .filter(headerDate -> !parameter.valueAs(headerDate, String.class, "").isEmpty())
                            .map(headerDate -> TimeSpentByDay.builder()
                                    .day(LocalDate.parse(headerDate))
                                    .timeSpentMinutes(parameter.valueAs(headerDate, Integer.class))
                                    .build())
                            .collect(Collectors.toList());

                    return new AbstractMap.SimpleEntry<>(taskId, timeSpentByDays);

                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<String, List<TimeSpentByDay>> taskIdAndTimeSpentByDaysForPerson = lastGeneratedPeopleView.getPersonMetrics().stream()
                .filter(personMetric -> person.equals(personMetric.getPerson()))
                .findFirst()
                .map(personMetric -> personMetric.getDailyTaskLogs().stream()
                        .collect(Collectors.toMap(
                                TaskWorkingLogMetrics::getTaskId,
                                TaskWorkingLogMetrics::getTimeSpentByDays)))
                .orElseThrow();

        assertThat(taskIdAndTimeSpentByDaysForPerson)
                .containsOnlyKeys(expectedTimeSpentByDay.keySet());

        taskIdAndTimeSpentByDaysForPerson.forEach((key, value) ->
            assertThat(value)
                    .withFailMessage("Task: %s", key)
                    .containsExactlyInAnyOrderElementsOf(expectedTimeSpentByDay.get(key)));
    }

    @Then("PeopleView PersonMetrics DailyTaskLogs Total TimeSpentByDay in Minutes for $person is: $totalTimeSpentByDayExamplesTable")
    public void thenPeopleViewPersonMetricsDailyTaskLogsTotalTimeSpentByDayInMinutesForUserIs(
            @Named("person") String person,
            @Named("timeSpentByDayexamplesTable") ExamplesTable totalTimeSpentByDayExamplesTable) {

        List<String> headersDates = totalTimeSpentByDayExamplesTable.getHeaders();

        List<TimeSpentByDay> expectedTotalTimeSpentByDay = totalTimeSpentByDayExamplesTable.getRowsAsParameters().stream()
                .map(parameter -> headersDates.stream()
                        .map(headerDate -> TimeSpentByDay.builder()
                                .day(LocalDate.parse(headerDate))
                                .timeSpentMinutes(parameter.valueAs(headerDate, Integer.class))
                                .build())
                        .collect(Collectors.toList()))
                .findFirst()
                .orElseThrow();

        List<TimeSpentByDay> totalTimeSpentByDayForPerson = lastGeneratedPeopleView.getPersonMetrics().stream()
                .filter(personMetric -> person.equals(personMetric.getPerson()))
                .findFirst()
                .map(PersonMetrics::getTotalTimeSpentByDay)
                .orElseThrow();

        assertThat(totalTimeSpentByDayForPerson)
                .containsExactlyInAnyOrderElementsOf(expectedTotalTimeSpentByDay);
    }
}
