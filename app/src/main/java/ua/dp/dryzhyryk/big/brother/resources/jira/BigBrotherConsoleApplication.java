package ua.dp.dryzhyryk.big.brother.resources.jira;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import lombok.extern.slf4j.Slf4j;
import ua.dp.dryzhyryk.big.brother.core.BigJiraBrotherPeopleViewProvider;
import ua.dp.dryzhyryk.big.brother.core.configuration.ConfigurationService;
import ua.dp.dryzhyryk.big.brother.core.data.source.JiraInformationHolder;
import ua.dp.dryzhyryk.big.brother.core.data.source.JiraInformationHolderImpl;
import ua.dp.dryzhyryk.big.brother.core.data.source.LogProxy;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.person.PeopleViewMetricsCalculator;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.person.TaskMetricsForPeopleCalculator;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.person.TaskMetricsForPeopleValidator;
import ua.dp.dryzhyryk.big.brother.core.ports.DataStorage;
import ua.dp.dryzhyryk.big.brother.core.ports.JiraResource;
import ua.dp.dryzhyryk.big.brother.core.utils.DateTimeProvider;
import ua.dp.dryzhyryk.big.brother.data.extractor.jira.JiraDataExtractor;
import ua.dp.dryzhyryk.big.brother.data.storage.jira.JiraFileDataStorage;
import ua.dp.dryzhyryk.big.brother.report.generator.ReportGenerator;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.ExcelReportGenerator;
import ua.dp.dryzhyryk.big.brother.resources.jira.inicialisation.Configurations;
import ua.dp.dryzhyryk.big.brother.resources.jira.processors.ReportByPersonProcessor;
import ua.dp.dryzhyryk.big.brother.resources.jira.search.PeopleSearchRequest;
import ua.dp.dryzhyryk.big.brother.resources.jira.search.SearchRequests;
import ua.dp.dryzhyryk.big.brother.resources.jira.utils.JsonUtils;

import java.io.File;
import java.util.List;

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

        AsynchronousJiraRestClientFactory jiraRestClientFactory = new AsynchronousJiraRestClientFactory();
        JiraRestClient jiraRestClient = jiraRestClientFactory
                .createWithBasicHttpAuthentication(config.getJiraUri(), config.getJiraUsername(), config.getJiraPassword());
        JiraResource jiraResource = new JiraDataExtractor(jiraRestClient);
        DataStorage jiraDataStorage = new JiraFileDataStorage(storageRoot.getAbsolutePath());
        JiraInformationHolder jiraInformationHolder = newJiraInformationHolder(jiraResource, jiraDataStorage, config);


        ConfigurationService configurationService = new ConfigurationService();
        TaskMetricsForPeopleCalculator taskMetricsForPeopleCalculator = new TaskMetricsForPeopleCalculator();
        TaskMetricsForPeopleValidator taskMetricsForPeopleValidator = new TaskMetricsForPeopleValidator(configurationService);
        PeopleViewMetricsCalculator peopleViewMetricsCalculator = new PeopleViewMetricsCalculator(taskMetricsForPeopleCalculator, taskMetricsForPeopleValidator);

        DateTimeProvider dateTimeProvider = newDateTimeProvider();

        BigJiraBrotherPeopleViewProvider bigJiraBrotherPeopleViewProvider = new BigJiraBrotherPeopleViewProvider(jiraInformationHolder, peopleViewMetricsCalculator);

        ReportGenerator reportGenerator = new ExcelReportGenerator(reportRoot.getAbsolutePath());

        reportByPersonProcessor = new ReportByPersonProcessor(
                bigJiraBrotherPeopleViewProvider,
                reportGenerator,
                dateTimeProvider);
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
