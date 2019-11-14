package ua.dp.dryzhyryk.big.brother.resources.jira.api;

import java.text.DateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.TimeTracking;

import ua.dp.dryzhyryk.big.brother.core.model.Task;
import ua.dp.dryzhyryk.big.brother.core.ports.JiraResource;

@Resource
public class JiraResourceImpl implements JiraResource {

	private static final DateTimeFormatter DATA_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
	private final JiraRestClient jiraRestClient;

	@Autowired
	public JiraResourceImpl(JiraRestClient jiraRestClient) {
		this.jiraRestClient = jiraRestClient;
	}

	@Override
	public List<Task> loadDayForProject(String projectKey, LocalDate date) {



		String jql = String.format("project = %s  AND worklogDate >=  '%s'", projectKey, date.format(DATA_FORMATTER));

		List<Issue> rootIssues = loadRootIssues(jql);

		String issueKeykey = issue.getKey();
		//				issue.getSubtasks().forEach(sub -> {
		//					printIssue(jiraRestClient, dateFormatter, sub.getIssueKey(), "              ");
		//
		//				});
	}

	private List<Issue> loadRootIssues(String jql) {
		int oneStepSize = 5;
		int index = 0;

		List<Issue> rootIssues = new ArrayList<>();
		do {
			SearchResult result = jiraRestClient.getSearchClient()
					.searchJql(jql, oneStepSize, index * oneStepSize, null).claim();

			index++;

			result.getIssues().forEach(issue -> {
				rootIssues.add(issue);
				printIssue(jiraRestClient, dateFormatter, issueKeykey, "");
			});

			if (!result.getIssues().iterator().hasNext()) {
				break;
			}
		}
		while (true);
		return null;
	}

	private void printIssue(JiraRestClient jiraRestClient, DateFormat dateFormatter, String key, String prefix) {

		Issue fullIssue = jiraRestClient.getIssueClient().getIssue(key).claim();

		System.out.println(prefix + key + "     " + fullIssue.getSummary());
		TimeTracking timeTracking = fullIssue.getTimeTracking();
		System.out.println(prefix
				+ minutesToHours(timeTracking.getOriginalEstimateMinutes()) + "     "
				+ minutesToHours(timeTracking.getRemainingEstimateMinutes()) + "     "
				+ minutesToHours(timeTracking.getTimeSpentMinutes()));
		//		System.out.println(prefix + dateFormatter.format(fullIssue.getCreationDate().toDate())
		//				+ "  =>   " + dateFormatter.format(fullIssue.getUpdateDate().toDate())
		//				+ "  =>   " + (null != fullIssue.getDueDate() ? dateFormatter.format(fullIssue.getDueDate().toDate()) : ""));

		fullIssue.getWorklogs().forEach(worklog -> {
			System.out.println("		"
					+ prefix
					+ dateFormatter.format(worklog.getUpdateDate().toDate()) + "   "
					+ dateFormatter.format(worklog.getStartDate().toDate()) + "   "
					+ worklog.getUpdateAuthor().getName() + "  "
					+ minutesToHours(worklog.getMinutesSpent()));
		});
	}
}
