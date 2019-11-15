package ua.dp.dryzhyryk.big.brother.resources.jira.api;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.TimeTracking;
import com.atlassian.jira.rest.client.api.domain.Worklog;

import reactor.core.publisher.Flux;
import ua.dp.dryzhyryk.big.brother.core.model.Task;
import ua.dp.dryzhyryk.big.brother.core.model.TaskWorkLog;
import ua.dp.dryzhyryk.big.brother.core.model.search.SprintSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.ports.JiraResource;

@Repository
public class JiraResourceImpl implements JiraResource {

	private static final int ONE_STEP_SIZE = 5;
	private static final int TIMOUT_IN_SECONDS = 10;
	private static final long REQUEST_RETRIES_NUMBER = 3;

	private final JiraRestClient jiraRestClient;

	@Autowired
	public JiraResourceImpl(JiraRestClient jiraRestClient) {
		this.jiraRestClient = jiraRestClient;
	}

	@Override
	public List<Task> loadProjectSprint(SprintSearchConditions sprintSearchConditions) {

		String jql = String.format("project = '%s' AND sprint = '%s' AND type != Sub-task",
				sprintSearchConditions.getProject(),
				sprintSearchConditions.getSprint());

		List<Issue> rootIssues = loadRootIssues(jql);

		System.out.println("===>>> " + rootIssues);

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

	private List<Issue> loadRootIssues(String jql) {
		return Flux.<List<Issue>, Integer> generate(() -> 0, (index, sink) -> {
			//TODO add retry on exception
			SearchResult result = jiraRestClient.getSearchClient().searchJql(jql, ONE_STEP_SIZE, index, null).claim();

			List<Issue> issues = StreamSupport.stream(result.getIssues().spliterator(), false)
					.collect(Collectors.toList());

			if (issues.isEmpty()) {
				sink.complete();
				//				issues.forEach(sink::next);
			} else {
				sink.next(issues);
			}
			return index + ONE_STEP_SIZE;
		}) //.retry(REQUEST_RETRIES_NUMBER)
				.flatMap(issues -> Flux.fromStream(issues.stream()))
				.toStream()
				.collect(Collectors.toList());
	}

	private Issue loadIssueFully(String issueKey) {

		System.out.println("===>>> fully " + issueKey);

		try {
			//TODO add retry on exception
			return jiraRestClient.getIssueClient().getIssue(issueKey).get();//.get(TIMOUT_IN_SECONDS, TimeUnit.SECONDS);
		}
		catch (Exception e) {
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
				.build();
	}

	private TaskWorkLog toTaskWorkLog(Worklog worklog) {
		LocalDateTime dateTime = LocalDateTime.parse(
				worklog.getStartDate().toDateTime().toString("yyyy-MM-dd'T'HH:mm:ssZ"),
				DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ"));

		return TaskWorkLog
				.builder()
				.person(worklog.getUpdateAuthor().getName())
				.startDateTime(dateTime)
				.minutesSpent(worklog.getMinutesSpent())
				.build();
	}
}
