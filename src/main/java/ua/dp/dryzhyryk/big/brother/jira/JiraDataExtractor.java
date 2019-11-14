package ua.dp.dryzhyryk.big.brother.jira;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.ProjectRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.TimeTracking;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

import io.atlassian.util.concurrent.Promise;

@Component
public class JiraDataExtractor {

	private final Log log = LogFactory.getLog(this.getClass());

	//	@PostConstruct
	//	public void init() {
	//		this.extract();
	//	}

	public static void main(String[] args) {
		new JiraDataExtractor().extract();
	}

	public void extract() {

		URI uri = URI.create("https://jira.dp.ua");
		String username = "mega_user";
		String password = "mega_password";

		AsynchronousJiraRestClientFactory jiraRestClientFactory = new AsynchronousJiraRestClientFactory();
		JiraRestClient jiraRestClient = jiraRestClientFactory.createWithBasicHttpAuthentication(uri, username, password);

		//		showAllProjects(jiraRestClient);

		//BasicProject{self=https://jira.dp.ua/rest/api/2/project/18271, key=PRJ_KEY, id=18271, name=SWE Forex Post Trading Evolutions}

		//		SearchResult result = jiraRestClient.getSearchClient().searchJql("project='SWE Forex Post Trading Evolutions'", 10, 0, null).claim();
		//		Set<String> fields = null; //Stream.of("worklog", "summary", "issuetype", "created", "updated").collect(Collectors.toSet());
		//		SearchResult result = jiraRestClient.getSearchClient().searchJql("key=PRJ_KEY-95", 10, 0, fields).claim();
		//		System.out.println("=================================");
		//		//		result.forEach(System.out::println);
		//		System.out.println(result.getIssues().iterator().next().getLabels());
		//
		//		result.getIssues().forEach(issue -> {
		//			System.out.println(issue.getKey() + "     " + issue.getSummary());
		//
		//			Issue fullIssue = jiraRestClient.getIssueClient().getIssue(issue.getKey()).claim();
		//
		//			fullIssue.getWorklogs().forEach(worklog -> {
		//				System.out.println("		" + worklog.getUpdateAuthor().getName() + "  " + worklog.getMinutesSpent());
		//			});
		//		});

		//		loadIssues("project=PRJ_KEY", jiraRestClient);
		//		loadIssues("project=PRJ_KEY AND worklogDate >= '2019/11/01' ", jiraRestClient);
		//		loadIssues("project=PRJ_KEY AND sprint = 'Ducks - RIC Always' AND type != Sub-task", jiraRestClient);

		//		loadIssues("project = PRJ_KEY  AND updatedDate >= '2019/11/04' AND updatedDate <= '2019/11/05'", jiraRestClient);

		loadIssues("project = PRJ_KEY  AND worklogDate >=  '2019/11/04'", jiraRestClient);

	}

	private void loadIssues(String jql, JiraRestClient jiraRestClient) {

		int oneStepSize = 5;
		int index = 0;

		DateFormat dateFormatter = SimpleDateFormat.getDateTimeInstance();

		do {
			SearchResult result = jiraRestClient.getSearchClient()
					.searchJql(jql, oneStepSize, index * oneStepSize, null).claim();

			index++;

			result.getIssues().forEach(issue -> {
				String key = issue.getKey();
				printIssue(jiraRestClient, dateFormatter, key, "");

//				issue.getSubtasks().forEach(sub -> {
//					printIssue(jiraRestClient, dateFormatter, sub.getIssueKey(), "              ");
//
//				});
			});

			if (!result.getIssues().iterator().hasNext() /*|| index == 1*/) {
				break;
			}
		}
		while (true);
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

	private Integer minutesToHours(Integer minutes) {
		if (null == minutes) {
			return null;
		}
		return minutes / 60;
	}

	private void showAllProjects(JiraRestClient jiraRestClient) {
		ProjectRestClient projectsClient = jiraRestClient.getProjectClient();
		Promise<Iterable<BasicProject>> projectsPromise = projectsClient.getAllProjects();

		projectsPromise.claim().forEach(System.out::println);
	}
}
