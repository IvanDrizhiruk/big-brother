package ua.dp.dryzhyryk.big.brother.data.extractor.jira;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.TimeTracking;
import com.atlassian.jira.rest.client.api.domain.Worklog;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import ua.dp.dryzhyryk.big.brother.core.ports.JiraResource;
import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.data.Task;
import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.data.TaskWorkLog;
import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.search.conditions.JiraSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.search.conditions.types.JiraPersonSearchConditions;
import ua.dp.dryzhyryk.big.brother.data.extractor.jira.extention.JiraRestClientExtended;

@Slf4j
public class JiraDataExtractor implements JiraResource {

	private static final String DATETIME_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ssZ";
	private static final int ONE_STEP_SIZE = 5;
	private static final int TIMOUT_IN_SECONDS = 10;
	private static final long REQUEST_RETRIES_NUMBER = 3;
	private static final DateTimeFormatter DATA_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private final JiraRestClientExtended jiraRestClient;
	private final JiraExtraConfiguration configuration;
	//TODO can be issue with memory. Should be reworked to expandable cache
	private final Map<String, Task> taskCache = new HashMap<>();

	public JiraDataExtractor(JiraRestClientExtended jiraRestClient, JiraExtraConfiguration configuration) {
		this.jiraRestClient = jiraRestClient;
		this.configuration = configuration;
	}

	@Override
	public List<Task> loadTasks(JiraSearchConditions searchConditions) {

		switch (searchConditions.getSearchConditionType()) {
			case PERSON:
				return getTasksForPerson((JiraPersonSearchConditions) searchConditions);
		}

		throw new IllegalArgumentException(
				"Unable to load tasks. Unsupported search type " + searchConditions.getSearchConditionType());
	}

	private List<Task> getTasksForPerson(JiraPersonSearchConditions searchConditions) {

		String jql = String.format("worklogAuthor = %s AND  worklogDate >=  %s AND  worklogDate <= %s",
				searchConditions.getPersonName(),
				searchConditions.getStartPeriod().format(DATA_FORMATTER),
				searchConditions.getEndPeriod().format(DATA_FORMATTER));

		List<Issue> issues = loadIssues(jql);

		log.info("Root task has loaded {} ", issues);

		return issues.stream()
				.map(issue -> taskCache.computeIfAbsent(issue.getKey(), key -> toTask(loadIssueFully(key))))
				.collect(Collectors.toList());
	}

	private List<Issue> loadIssues(String jql) {
		return Flux.<List<Issue>, Integer> generate(() -> 0, (index, sink) -> {
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

	@SneakyThrows
	private Issue loadIssueFully(String issueKey) {

		log.info("Load issue fully {} ", issueKey);

		//TODO add retry on exception
		Issue issue = jiraRestClient.getIssueClient().getIssue(issueKey).get();

		Collection<Worklog> fullWorkLogs = jiraRestClient.getWorklogsClient().getWorkLogs(issueKey).get();

		List<Worklog> workLogs = ((List<Worklog>) issue.getWorklogs());
		workLogs.clear();
		workLogs.addAll(fullWorkLogs);

		return issue;//.get(TIMOUT_IN_SECONDS, TimeUnit.SECONDS);
	}

	private Task toTask(Issue issue) {
		TimeTracking timeTracking = issue.getTimeTracking();

		List<TaskWorkLog> taskWorkLogs = StreamSupport.stream(issue.getWorklogs().spliterator(), false)
				.map(this::toTaskWorkLog)
				.collect(Collectors.toList());

		Map<String, String> additionalFieldValues = configuration.getFieldNamesForLoading().stream()
				.collect(Collectors.toMap(
						Function.identity(),
						field -> extractFieldAsString(issue, field)));

		return Task.builder()
				.id(issue.getKey())
				.name(issue.getSummary())
				.type(issue.getIssueType().getName())
				.isSubTask(issue.getIssueType().isSubtask())
				.additionalFieldValues(additionalFieldValues)
				.status(issue.getStatus().getName())
				.originalEstimateMinutes(timeTracking.getOriginalEstimateMinutes())
				.remainingEstimateMinutes(timeTracking.getRemainingEstimateMinutes())
				.timeSpentMinutes(timeTracking.getTimeSpentMinutes())
				.workLogs(taskWorkLogs)
				.subTasks(new ArrayList<>())
				.build();
	}

	private String extractFieldAsString(Issue issue, String fieldName) {
		try {
			IssueField field = issue.getFieldByName(fieldName);
			if (null == field) {
				log.warn("Unable find {} for task {}", fieldName, issue.getId());
				return null;
			}
			JSONObject jsonObjectValue = (JSONObject) field.getValue();
			return jsonObjectValue == null
					? ""
					: jsonObjectValue.getString("value");
		}
		catch (JSONException e) {

			log.error("Unable extract additional field " + fieldName + " for issue " + issue.getSummary() + " ", e);
			return null;
		}
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
