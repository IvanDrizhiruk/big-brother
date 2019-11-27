package ua.dp.dryzhyryk.big.brother.resources.jira;

import java.io.File;
import java.time.LocalDate;
import java.util.Arrays;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

import lombok.extern.slf4j.Slf4j;
import ua.dp.dryzhyryk.big.brother.core.BigJiraBrother;
import ua.dp.dryzhyryk.big.brother.core.BigJiraBrotherPeopleView;
import ua.dp.dryzhyryk.big.brother.core.data.source.JiraInformationHolder;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.PeopleSearchConditions;
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

		Configurations config = Configurations.loadFromAppArguments(args);

		File storageRoot = new File(config.getRootDir(), "storage");
		storageRoot.mkdirs();

		File reportRoot = new File(config.getRootDir(), "/reports");
		reportRoot.mkdirs();

		String searchFilePath = config.getRootDir() + "/search.json";

		AsynchronousJiraRestClientFactory jiraRestClientFactory = new AsynchronousJiraRestClientFactory();
		JiraRestClient jiraRestClient = jiraRestClientFactory
				.createWithBasicHttpAuthentication(config.getJiraUri(), config.getJiraUsername(), config.getJiraPassword());
		JiraResource jiraResource = new JiraDataExtractor(jiraRestClient);
		JiraDataStorage jiraDataStorage = new JiraFileDataStorage(storageRoot.getAbsolutePath());
		JiraInformationHolder jiraInformationHolder = new JiraInformationHolder(jiraResource, jiraDataStorage);
		TasksTreeViewMetricsCalculator tasksTreeViewMetricsCalculator = new TasksTreeViewMetricsCalculator();
		PeopleViewMetricsCalculator peopleViewMetricsCalculator = new PeopleViewMetricsCalculator();
		SprintViewMetricsCalculator sprintViewMetricsCalculator = new SprintViewMetricsCalculator();
		BigJiraBrother bigJiraBrother = new BigJiraBrother(jiraInformationHolder, tasksTreeViewMetricsCalculator, peopleViewMetricsCalculator,
				sprintViewMetricsCalculator);

		BigJiraBrotherPeopleView bigJiraBrotherPeopleView = new BigJiraBrotherPeopleView(jiraInformationHolder, peopleViewMetricsCalculator);

		ExcelReportGenerator reportGenerator = new ExcelReportGenerator(reportRoot.getAbsolutePath());

		SprintSearchConditions sprintSearchConditions = JsonUtils.loadJson(searchFilePath, SprintSearchConditions.class);
		PeopleSearchConditions peopleSearchConditions = PeopleSearchConditions.builder()
				.teamName("Ducks")
				.peopleNames(Arrays.asList("o_dkoval", "o_ssolov", "o_okolom", "o_ystepa", "o_izhytn"))
				.startPeriod(LocalDate.of(2019, 11, 11))
				.endPeriod(LocalDate.of(2019, 11, 15))
				.build();

		prepareReportBySprint(sprintSearchConditions, bigJiraBrother, reportGenerator);
		prepareReportByPerson(peopleSearchConditions, bigJiraBrotherPeopleView, reportGenerator);
	}

	private static void prepareReportByPerson(PeopleSearchConditions peopleSearchConditions, BigJiraBrotherPeopleView bigJiraBrotherPeopleView,
			ExcelReportGenerator reportGenerator) {
		PeopleView peopleView = bigJiraBrotherPeopleView.preparePeopleView(peopleSearchConditions);
		reportGenerator.generateReport(peopleView);
	}

	private static void prepareReportBySprint(SprintSearchConditions sprintSearchConditions, BigJiraBrother bigJiraBrother,
			ExcelReportGenerator reportGenerator) {
		TasksTreeView tasksTreeView = bigJiraBrother.prepareTaskView(sprintSearchConditions);
		SprintView sprintView = bigJiraBrother.prepareSprintView(sprintSearchConditions);
		reportGenerator.generateReport(tasksTreeView, sprintView);
	}
}
