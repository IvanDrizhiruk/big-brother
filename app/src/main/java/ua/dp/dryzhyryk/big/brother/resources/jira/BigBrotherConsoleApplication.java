package ua.dp.dryzhyryk.big.brother.resources.jira;

import java.io.File;
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
import ua.dp.dryzhyryk.big.brother.core.data.source.JiraInformationHolder;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.SprintSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.PeopleViewMetricsCalculator;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.SprintViewMetricsCalculator;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.TasksTreeViewMetricsCalculator;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.PeopleView;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.SprintView;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.TasksTreeView;
import ua.dp.dryzhyryk.big.brother.core.ports.JiraDataStorage;
import ua.dp.dryzhyryk.big.brother.core.ports.JiraResource;
import ua.dp.dryzhyryk.big.brother.data.extractor.jira.JiraDataExtractor;
import ua.dp.dryzhyryk.big.brother.data.storage.jira.JiraFileDataStorage;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.ExcelReportGenerator;

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

		File storageRoot = new File(configDir.orElse("./"), "../storage");
		storageRoot.mkdirs();

		JiraResource jiraResource = new JiraDataExtractor(jiraRestClient);
		JiraDataStorage jiraDataStorage = new JiraFileDataStorage(storageRoot.getPath());
		JiraInformationHolder jiraInformationHolder = new JiraInformationHolder(jiraResource, jiraDataStorage);
		TasksTreeViewMetricsCalculator tasksTreeViewMetricsCalculator = new TasksTreeViewMetricsCalculator();
		PeopleViewMetricsCalculator peopleViewMetricsCalculator = new PeopleViewMetricsCalculator();
		SprintViewMetricsCalculator sprintViewMetricsCalculator = new SprintViewMetricsCalculator();
		BigJiraBrother bigJiraBrother = new BigJiraBrother(jiraInformationHolder, tasksTreeViewMetricsCalculator, peopleViewMetricsCalculator,
				sprintViewMetricsCalculator);

		String serchFilePath = configDir
				.map(path -> path + "/search.json")
				.orElse("search.json");

		SprintSearchConditions sprintSearchConditions = loadJson(serchFilePath, SprintSearchConditions.class);

		TasksTreeView tasksTreeView = bigJiraBrother.prepareTaskView(sprintSearchConditions);
		PeopleView peopleView = bigJiraBrother.preparePeopleView(sprintSearchConditions);
		SprintView sprintView = bigJiraBrother.prepareSprintView(sprintSearchConditions);


		File reportRoot = new File(configDir.orElse("./"), "../reports");
		reportRoot.mkdirs();
		ExcelReportGenerator reportGenerator = new ExcelReportGenerator(reportRoot.getAbsolutePath());
		reportGenerator.generateReport(tasksTreeView, peopleView, sprintView);
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
		try (InputStream input = new FileInputStream(fileName)) {

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
