package ua.dp.dryzhyryk.big.brother.resources.jira;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import lombok.extern.slf4j.Slf4j;
import ua.dp.dryzhyryk.big.brother.core.BigJiraBrother;
import ua.dp.dryzhyryk.big.brother.core.BigJiraBrotherPeopleView;
import ua.dp.dryzhyryk.big.brother.core.data.source.JiraInformationHolder;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.PeopleViewMetricsCalculator;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.SprintViewMetricsCalculator;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.TasksRootViewMetricsCalculator;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.TasksTreeViewMetricsCalculator;
import ua.dp.dryzhyryk.big.brother.core.ports.JiraDataStorage;
import ua.dp.dryzhyryk.big.brother.core.ports.JiraResource;
import ua.dp.dryzhyryk.big.brother.data.extractor.jira.JiraDataExtractor;
import ua.dp.dryzhyryk.big.brother.data.storage.jira.JiraFileDataStorage;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.ExcelReportGenerator;
import ua.dp.dryzhyryk.big.brother.resources.jira.inicialisation.Configurations;
import ua.dp.dryzhyryk.big.brother.resources.jira.processors.ReportByPersonProcessor;
import ua.dp.dryzhyryk.big.brother.resources.jira.processors.ReportBySprintProcessor;
import ua.dp.dryzhyryk.big.brother.resources.jira.search.SearchRequests;
import ua.dp.dryzhyryk.big.brother.resources.jira.utils.JsonUtils;

import java.io.File;

@Slf4j
public class BigBrotherConsoleApplication {


	private static Configurations config;
	private static ReportBySprintProcessor reportBySprintProcessor;
	private static ReportByPersonProcessor reportByPersonProcessor;

	private static void initBeans(String[] args) {

		config = Configurations.loadFromAppArguments(args);

		File storageRoot = new File(config.getRootDir(), "storage");
		storageRoot.mkdirs();

		File reportRoot = new File(config.getRootDir(), "/reports");
		reportRoot.mkdirs();

        AsynchronousJiraRestClientFactory jiraRestClientFactory = new AsynchronousJiraRestClientFactory();
        JiraRestClient jiraRestClient = jiraRestClientFactory
                .createWithBasicHttpAuthentication(config.getJiraUri(), config.getJiraUsername(), config.getJiraPassword());
        JiraResource jiraResource = new JiraDataExtractor(jiraRestClient);
        JiraDataStorage jiraDataStorage = new JiraFileDataStorage(storageRoot.getAbsolutePath());
        JiraInformationHolder jiraInformationHolder = new JiraInformationHolder(jiraResource, jiraDataStorage);
        TasksTreeViewMetricsCalculator tasksTreeViewMetricsCalculator = new TasksTreeViewMetricsCalculator();
        TasksRootViewMetricsCalculator tasksRootViewMetricsCalculator = new TasksRootViewMetricsCalculator();
        PeopleViewMetricsCalculator peopleViewMetricsCalculator = new PeopleViewMetricsCalculator();
        SprintViewMetricsCalculator sprintViewMetricsCalculator = new SprintViewMetricsCalculator();
        BigJiraBrother bigJiraBrother = new BigJiraBrother(jiraInformationHolder, tasksTreeViewMetricsCalculator, tasksRootViewMetricsCalculator,
                sprintViewMetricsCalculator);

        BigJiraBrotherPeopleView bigJiraBrotherPeopleView = new BigJiraBrotherPeopleView(jiraInformationHolder, peopleViewMetricsCalculator);

        ExcelReportGenerator reportGenerator = new ExcelReportGenerator(reportRoot.getAbsolutePath());

        reportBySprintProcessor = new ReportBySprintProcessor(bigJiraBrother, reportGenerator);
        reportByPersonProcessor = new ReportByPersonProcessor(bigJiraBrotherPeopleView, reportGenerator);
    }

    public static void main(String[] args) {

        initBeans(args);

		SearchRequests searchRequests = loadSearchRequests();

		reportBySprintProcessor.prepareReportBySprint(searchRequests.getSprintSearchConditions());
        reportByPersonProcessor.prepareReportByPerson(searchRequests.getPeopleSearchConditions());
    }

	private static SearchRequests loadSearchRequests() {
		String searchFilePath = config.getRootDir() + "/search.json";
		return JsonUtils.loadJson(searchFilePath, SearchRequests.class);
	}

}
