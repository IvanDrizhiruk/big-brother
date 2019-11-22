package ua.dp.dryzhyryk.big.brother.resources.jira;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
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
import ua.dp.dryzhyryk.big.brother.core.data.source.model.Task;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.SprintSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.MetricksCalculator;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.TaskMetrics;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.TasksTree;
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
		MetricksCalculator metricksCalculator = new MetricksCalculator();
		BigJiraBrother bigJiraBrother = new BigJiraBrother(jiraInformationHolder, metricksCalculator);

		String serchFilePath = configDir
				.map(path -> path + "/search.json")
				.orElse("search.json");

		SprintSearchConditions sprintSearchConditions = loadJson(serchFilePath, SprintSearchConditions.class);

		TasksTree tasksTree = bigJiraBrother.prepareTaskView(sprintSearchConditions);


		File reportRoot = new File(configDir.orElse("./"), "../reports");
		reportRoot.mkdirs();
		ExcelReportGenerator reportGenerator = new ExcelReportGenerator(reportRoot.getAbsolutePath());
		reportGenerator.generate(tasksTree);


		List<Task> rootTasks = tasksTree.getRootTasks();
		Map<String, TaskMetrics> taskMetricsByTaskId = tasksTree.getTaskMetricsByTaskId();
		rootTasks.forEach(task -> {
			System.out.println(task.getId() + " " + task.getName());

			TaskMetrics taskMetric = taskMetricsByTaskId.get(task.getId());
			System.out.println(
					"Estimated " + convertMinutesToHour(taskMetric.getTimeMetrics().getOriginalEstimateMinutes()) +
							" Real " + convertMinutesToHour(taskMetric.getTimeMetrics().getTimeSpentMinutes()) +
							" Remaining " + convertMinutesToHour(taskMetric.getTimeMetrics().getRemainingEstimateMinutes()) +
							"  TC 			==> " + taskMetric.getTimeMetrics().getTimeCoefficient());

			taskMetric.getDailyWorkLog().forEach(dayWorkLog -> {

				System.out.println(dayWorkLog.getWorkDate());
				dayWorkLog.getPersonWorkLogs().forEach(personWorkLog -> {
					System.out.println(personWorkLog.getPerson() + " " + convertMinutesToHour(personWorkLog.getMinutesSpent()));
				});

			});

			task.getSubTasks().forEach(subTask -> {

				TaskMetrics subMaskMetric = taskMetricsByTaskId.get(subTask.getId());

				System.out.println("          " + subTask.getId() + " " + subTask.getName());

				System.out.println("          " +
						"Estimated " + convertMinutesToHour(subMaskMetric.getTimeMetrics().getOriginalEstimateMinutes()) +
						" Real " + convertMinutesToHour(subMaskMetric.getTimeMetrics().getTimeSpentMinutes()) +
						" Remaining " + convertMinutesToHour(subMaskMetric.getTimeMetrics().getRemainingEstimateMinutes()) +
						"  TC 			==> " + subMaskMetric.getTimeMetrics().getTimeCoefficient());

				subMaskMetric.getDailyWorkLog().forEach(dayWorkLog -> {

					System.out.println("          " + dayWorkLog.getWorkDate());
					dayWorkLog.getPersonWorkLogs().forEach(personWorkLog -> {
						System.out
								.println("          " + personWorkLog.getPerson() + " " + convertMinutesToHour(personWorkLog.getMinutesSpent()));
					});

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
