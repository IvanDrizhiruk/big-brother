package ua.dp.dryzhyryk.big.brother.data.extractor.jira;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.TimeTracking;
import com.atlassian.jira.rest.client.api.domain.Worklog;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.Task;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.TaskWorkLog;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.PersonSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.SearchConditions;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.SprintSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.ports.JiraResource;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
public class JiraDataExtractor implements JiraResource {

    private static final String DATETIME_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ssZ";
    private static final int ONE_STEP_SIZE = 5;
    private static final int TIMOUT_IN_SECONDS = 10;
    private static final long REQUEST_RETRIES_NUMBER = 3;
    private static final DateTimeFormatter DATA_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final JiraRestClient jiraRestClient;

    public JiraDataExtractor(JiraRestClient jiraRestClient) {
        this.jiraRestClient = jiraRestClient;
    }

    @Override
    public List<Task> loadTasks(SearchConditions searchConditions) {

        switch(searchConditions.getSearchConditionType()) {
            case SPRINT:
                return getTasksForSprint((SprintSearchConditions)searchConditions);
            case PERSON:
                return getTasksForPerson((PersonSearchConditions)searchConditions);
        }

        throw new IllegalArgumentException(
                "Unable to load tasks. Unsupported search type " + searchConditions.getSearchConditionType());
    }


    private List<Task> getTasksForSprint(SprintSearchConditions searchConditions) {
        String jql = String.format("project = '%s' AND sprint = '%s' AND issuetype in standardIssueTypes()",
                searchConditions.getProject(),
                searchConditions.getSprint());

        List<Issue> rootIssues = loadIssues(jql);

        log.info("Root task has loaded {} ", rootIssues);

        return rootIssues.stream()
                .map(issue -> loadIssueFully(issue.getKey()))
                .map(fullIssue -> {
                    List<Task> subIssues = StreamSupport.stream(fullIssue.getSubtasks().spliterator(), false)
                            .map(subIssue -> loadIssueFully(subIssue.getIssueKey()))
                            .map(this::toTask)
                            .collect(Collectors.toList());

                    return toTask(fullIssue)
                            .toBuilder()
                            .subTasks(subIssues)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<Task> getTasksForPerson(PersonSearchConditions searchConditions) {

        String jql = String.format("worklogAuthor = %s AND  worklogDate >=  %s AND  worklogDate <= %s",
                searchConditions.getPersonName(),
                searchConditions.getStartPeriod().format(DATA_FORMATTER),
                searchConditions.getEndPeriod().format(DATA_FORMATTER));

        List<Issue>  issues = loadIssues(jql);

        log.info("Root task has loaded {} ",  issues);

        return  issues.stream()
                .map(issue -> loadIssueFully(issue.getKey()))
                .map(this::toTask)
                .collect(Collectors.toList());
    }


    private List<Issue> loadIssues(String jql) {
        return Flux.<List<Issue>, Integer>generate(() -> 0, (index, sink) -> {
            //TODO add retry on exception
            SearchResult result = jiraRestClient.getSearchClient().searchJql(jql, ONE_STEP_SIZE, index, null).claim();

            List<Issue> issues = StreamSupport.stream(result.getIssues().spliterator(), false)
                    .collect(Collectors.toList());

            if (issues.isEmpty()) {
                sink.complete();
            } else {
                sink.next(issues);
            }
            return index + ONE_STEP_SIZE;
        }).retry(REQUEST_RETRIES_NUMBER)
                .flatMap(issues -> Flux.fromStream(issues.stream()))
                .toStream()
                .collect(Collectors.toList());
    }

    private Issue loadIssueFully(String issueKey) {

        log.info("Load issue fully {} ", issueKey);

        try {
            //TODO add retry on exception
            return jiraRestClient.getIssueClient().getIssue(issueKey).get();//.get(TIMOUT_IN_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Task toTask(Issue issue) {
        TimeTracking timeTracking = issue.getTimeTracking();

        List<TaskWorkLog> taskWorkLogs = StreamSupport.stream(issue.getWorklogs().spliterator(), false)
                .map(this::toTaskWorkLog)
                .collect(Collectors.toList());

        return Task.builder()
                .id(issue.getKey())
                .name(issue.getSummary())
                .originalEstimateMinutes(timeTracking.getOriginalEstimateMinutes())
                .remainingEstimateMinutes(timeTracking.getRemainingEstimateMinutes())
                .timeSpentMinutes(timeTracking.getTimeSpentMinutes())
                .workLogs(taskWorkLogs)
                .subTasks(new ArrayList<>())
                .build();
    }

    private TaskWorkLog toTaskWorkLog(Worklog worklog) {
        LocalDateTime dateTime = LocalDateTime.parse(
                worklog.getStartDate().toDateTime().toString(DATETIME_FORMAT_PATTERN),
                DateTimeFormatter.ofPattern(DATETIME_FORMAT_PATTERN));

        return TaskWorkLog
                .builder()
                .person(worklog.getUpdateAuthor().getName())
                .startDateTime(dateTime)
                .minutesSpent(worklog.getMinutesSpent())
                .build();
    }
}
