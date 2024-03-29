package ua.dp.dryzhyryk.big.brother.resources.jira;

import java.io.File;
import java.util.List;

import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.AsynchronousHttpClientFactory;
import com.atlassian.jira.rest.client.internal.async.DisposableHttpClient;

import lombok.extern.slf4j.Slf4j;
import ua.dp.dryzhyryk.big.brother.core.BigJiraBrotherPeopleViewProvider;
import ua.dp.dryzhyryk.big.brother.core.calculator.task.metrics.TaskMetricsForPersonCalculator;
import ua.dp.dryzhyryk.big.brother.core.calculator.task.metrics.TasksMetricsForPersonCalculator;
import ua.dp.dryzhyryk.big.brother.core.calculator.task.metrics.validators.SpendTimeValidatorForFinishedTasks;
import ua.dp.dryzhyryk.big.brother.core.calculator.task.metrics.validators.SpendTimeValidatorForInProgressTasks;
import ua.dp.dryzhyryk.big.brother.core.calculator.task.metrics.validators.SpendTimeValidatorForNotFunctionalTasks;
import ua.dp.dryzhyryk.big.brother.core.calculator.task.work.log.TaskWorkingLogsForPeopleCalculator;
import ua.dp.dryzhyryk.big.brother.core.calculator.task.work.log.TaskWorkingLogsForPeopleValidator;
import ua.dp.dryzhyryk.big.brother.core.calculator.task.work.log.TasksWorkingLogsForPersonsCalculator;
import ua.dp.dryzhyryk.big.brother.core.configuration.ConfigurationService;
import ua.dp.dryzhyryk.big.brother.core.data.source.JiraInformationHolder;
import ua.dp.dryzhyryk.big.brother.core.data.source.JiraInformationHolderImpl;
import ua.dp.dryzhyryk.big.brother.core.data.source.LogProxy;
import ua.dp.dryzhyryk.big.brother.core.ports.DataStorage;
import ua.dp.dryzhyryk.big.brother.core.ports.JiraResource;
import ua.dp.dryzhyryk.big.brother.core.utils.DateTimeProvider;
import ua.dp.dryzhyryk.big.brother.data.extractor.jira.JiraDataExtractor;
import ua.dp.dryzhyryk.big.brother.data.extractor.jira.JiraExtraConfiguration;
import ua.dp.dryzhyryk.big.brother.data.extractor.jira.extention.JiraRestClientExtended;
import ua.dp.dryzhyryk.big.brother.data.extractor.jira.extention.JiraRestClientExtendedImpl;
import ua.dp.dryzhyryk.big.brother.data.storage.jira.JiraFileDataStorage;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.ExcelReportGenerator;
import ua.dp.dryzhyryk.big.brother.resources.jira.inicialisation.Configurations;
import ua.dp.dryzhyryk.big.brother.resources.jira.processors.ReportByPersonProcessor;
import ua.dp.dryzhyryk.big.brother.resources.jira.search.PeopleSearchRequest;
import ua.dp.dryzhyryk.big.brother.resources.jira.search.SearchRequests;
import ua.dp.dryzhyryk.big.brother.resources.jira.utils.JsonUtils;

@Slf4j
public class BigBrotherConsoleApplication {

	private final Configurations config;
	private final ReportByPersonProcessor reportByPersonProcessor;

	public BigBrotherConsoleApplication(Configurations config) {

		this.config = config;

		File storageRoot = new File(config.getRootDir(), "storage");
		storageRoot.mkdirs();

		File reportRoot = new File(config.getRootDir(), "/reports");
		reportRoot.mkdirs();

		JiraRestClientExtended jiraRestClient = newJiraRestClient(config);

		JiraExtraConfiguration jiraExtraConfiguration = JiraExtraConfiguration.builder()
				.fieldNamesForLoading(config.getFieldNamesForLoading())
				.build();

		JiraResource jiraResource = new JiraDataExtractor(jiraRestClient, jiraExtraConfiguration);
		DataStorage jiraDataStorage = new JiraFileDataStorage(storageRoot.getAbsolutePath());
		JiraInformationHolder jiraInformationHolder = newJiraInformationHolder(jiraResource, jiraDataStorage, config);

		ConfigurationService configurationService = new ConfigurationService();
		TaskWorkingLogsForPeopleCalculator taskMetricsForPeopleCalculator = new TaskWorkingLogsForPeopleCalculator();
		TaskWorkingLogsForPeopleValidator taskMetricsForPeopleValidator = new TaskWorkingLogsForPeopleValidator(configurationService);
		TasksWorkingLogsForPersonsCalculator peopleViewMetricsCalculator =
				new TasksWorkingLogsForPersonsCalculator(taskMetricsForPeopleCalculator, taskMetricsForPeopleValidator);

		DateTimeProvider dateTimeProvider = newDateTimeProvider();

		SpendTimeValidatorForInProgressTasks spendTimeValidatorForInProgressTasks = new SpendTimeValidatorForInProgressTasks();
		SpendTimeValidatorForFinishedTasks spendTimeValidatorForFinishedTasks = new SpendTimeValidatorForFinishedTasks();
		SpendTimeValidatorForNotFunctionalTasks spendTimeValidatorForNotFunctionalTasks = new SpendTimeValidatorForNotFunctionalTasks();
		TaskMetricsForPersonCalculator taskMetricsForPersonCalculator = new TaskMetricsForPersonCalculator(
				spendTimeValidatorForInProgressTasks,
				spendTimeValidatorForFinishedTasks,
				spendTimeValidatorForNotFunctionalTasks);

		TasksMetricsForPersonCalculator tasksMetricsForPersonCalculator = new TasksMetricsForPersonCalculator(
				taskMetricsForPersonCalculator);

		BigJiraBrotherPeopleViewProvider bigJiraBrotherPeopleViewProvider = new BigJiraBrotherPeopleViewProvider(
				jiraInformationHolder, peopleViewMetricsCalculator, tasksMetricsForPersonCalculator);

		ExcelReportGenerator reportGenerator = new ExcelReportGenerator(reportRoot.getAbsolutePath());

		reportByPersonProcessor = new ReportByPersonProcessor(
				bigJiraBrotherPeopleViewProvider,
				reportGenerator,
				dateTimeProvider);
	}

	private JiraRestClientExtended newJiraRestClient(Configurations config) {
		final DisposableHttpClient httpClient = new AsynchronousHttpClientFactory()
				.createClient(config.getJiraUri(), new BasicHttpAuthenticationHandler(config.getJiraUsername(), config.getJiraPassword()));

		return new JiraRestClientExtendedImpl(config.getJiraUri(), httpClient);
	}

	protected DateTimeProvider newDateTimeProvider() {
		return new DateTimeProvider();
	}

	protected JiraInformationHolder newJiraInformationHolder(JiraResource jiraResource, DataStorage jiraDataStorage,
			Configurations config) {
		JiraInformationHolder jiraInformationHolder = new JiraInformationHolderImpl(jiraResource, jiraDataStorage);
		return config.isDebugEnabled() ? new LogProxy(jiraInformationHolder) : jiraInformationHolder;
	}

	public static void main(String[] args) {

		Configurations config = Configurations.loadFromAppArguments(args);

		BigBrotherConsoleApplication app = new BigBrotherConsoleApplication(config);

		SearchRequests searchRequests = app.loadSearchRequests();

		app.prepareReportByPersonForLastFinishedWeek(searchRequests);
	}

	private SearchRequests loadSearchRequests() {
		String searchFilePath = config.getRootDir() + "/search.json";
		return JsonUtils.loadJson(searchFilePath, SearchRequests.class);
	}

	public void prepareReportByPersonForLastFinishedWeek(SearchRequests searchRequests) {
		List<PeopleSearchRequest> peopleSearchConditions = searchRequests.getPeopleSearchConditions();
		reportByPersonProcessor.prepareReportByPersonForLastFinishedWeek(peopleSearchConditions);
	}
}
