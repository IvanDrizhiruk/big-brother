package ua.dp.dryzhyryk.big.brother.resources.jira;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.extern.slf4j.Slf4j;
import ua.dp.dryzhyryk.big.brother.core.BigJiraBrother;
import ua.dp.dryzhyryk.big.brother.core.data.source.JiraInformationCache;
import ua.dp.dryzhyryk.big.brother.core.data.source.JiraInformationHolder;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.TasksTree;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.SprintSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.ports.JiraResource;
import ua.dp.dryzhyryk.big.brother.data.extractor.jira.JiraDataExtractor;

@Slf4j
public class BigBrotherConsoleApplication {

	public static void main(String[] args) {

		Optional<String> configDir = extract("configDir", args);

		String jiraFilePath = configDir
				.map(path -> path + "/jira.properties")
				.orElse("jira.properties");

		Properties jiraProperties = loadProperties(jiraFilePath);

		URI uri = URI.create(jiraProperties.getProperty("jira.url"));
		String username = jiraProperties.getProperty("jira.user");
		String password = jiraProperties.getProperty("jira.password");

		AsynchronousJiraRestClientFactory jiraRestClientFactory = new AsynchronousJiraRestClientFactory();
		JiraRestClient jiraRestClient = jiraRestClientFactory.createWithBasicHttpAuthentication(uri, username, password);

		JiraResource jiraResource = new JiraDataExtractor(jiraRestClient);
		JiraInformationCache jiraInformationCache = new JiraInformationCache(jiraResource);
		JiraInformationHolder jiraInformationHolder = new JiraInformationHolder(jiraInformationCache);
		BigJiraBrother bigJiraBrother = new BigJiraBrother(jiraInformationHolder);

		String serchFilePath = configDir
				.map(path -> path + "/search.json")
				.orElse("search.json");

		SprintSearchConditions sprintSearchConditions = loadJson(serchFilePath, SprintSearchConditions.class);

		TasksTree tasksTree = jiraInformationHolder.getTasksAsTree(sprintSearchConditions);

		//		Gson gson = (new GsonBuilder()).create();
		//
		//		System.out.println(gson.toJson(tasksTree));

		tasksTree.getRootTasks().forEach(task -> {
			System.out.println(task.getId() + " " + task.getName());
			System.out.println(
					"Estimated " + convertMinutesToHour(task.getOriginalEstimateMinutes()) +
							" Real " + convertMinutesToHour(task.getTimeSpentMinutes()) +
							" Remaining " + convertMinutesToHour(task.getRemainingEstimateMinutes()));

			task.getWorkLogs().forEach(worklog -> {
				System.out.println(worklog.getPerson() + " " + worklog.getMinutesSpent() + " " + worklog.getStartDateTime());
			});

			task.getSubTasks().forEach(subTask -> {
				System.out.println("          " + subTask.getId() + " " + subTask.getName());
				System.out.println("          " +
						"Estimated " + subTask.getOriginalEstimateMinutes() +
						" Real " + subTask.getTimeSpentMinutes() +
						" Remaining " + subTask.getRemainingEstimateMinutes());

				subTask.getWorkLogs().forEach(worklog -> {
					System.out.println("          " + worklog.getPerson() + " "
							+ convertMinutesToHour(worklog.getMinutesSpent()) + " "
							+ worklog.getStartDateTime());
				});

				System.out.println("");
			});
			System.out.println("");

		});
	}

	private static <T> T loadJson(String serchFilePath, Class<T> clazz) {
		Gson gson = (new GsonBuilder()).create();
		try (FileReader fileReader = new FileReader(serchFilePath)) {
			return gson.fromJson(fileReader, clazz);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static Optional<String> extract(String parameterName, String[] args) {
		return Stream.of(args)
				.filter(arg -> arg.startsWith(parameterName))
				.map(arg -> arg.substring(parameterName.length() + 1))
				.findFirst();
	}

	private static Properties loadProperties(String fileName) {
		try (InputStream input = new FileInputStream("/home/idryzhyruk/workspace/luxoft/big-brother/config/jira.properties")) {

			if (input == null) {
				log.error("Sorry, unable to find {}", fileName);
				throw new IllegalArgumentException("Sorry, unable to find " + fileName);
			}

			Properties prop = new Properties();
			prop.load(input);

			return prop;

		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static Integer convertMinutesToHour(Integer minutes) {
		if (null == minutes || minutes.equals(0)) {
			return 0;
		}
		return minutes / 60;
	}
}
