package ua.dp.dryzhyryk.big.brother.resources.jira;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import lombok.extern.slf4j.Slf4j;
import ua.dp.dryzhyryk.big.brother.core.BigJiraBrother;
import ua.dp.dryzhyryk.big.brother.core.BigJiraBrotherPeopleViewProviderOld;
import ua.dp.dryzhyryk.big.brother.core.data.source.JiraInformationHolder;
import ua.dp.dryzhyryk.big.brother.core.data.source.JiraInformationHolderImpl;
import ua.dp.dryzhyryk.big.brother.core.data.source.LogProxy;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.SprintViewMetricsCalculator;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.TasksRootViewMetricsCalculator;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.TasksTreeViewMetricsCalculator;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.person.PeopleViewMetricsCalculatorOld;
import ua.dp.dryzhyryk.big.brother.core.ports.JiraDataStorage;
import ua.dp.dryzhyryk.big.brother.core.ports.JiraResource;
import ua.dp.dryzhyryk.big.brother.core.ports.ReportGenerator;
import ua.dp.dryzhyryk.big.brother.core.utils.DateTimeProvider;
import ua.dp.dryzhyryk.big.brother.core.validator.ReportByPersonValidator;
import ua.dp.dryzhyryk.big.brother.data.extractor.jira.JiraDataExtractor;
import ua.dp.dryzhyryk.big.brother.data.storage.jira.JiraFileDataStorage;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.ExcelReportGenerator;
import ua.dp.dryzhyryk.big.brother.resources.jira.inicialisation.Configurations;
import ua.dp.dryzhyryk.big.brother.resources.jira.processors.ReportByPersonProcessor;
import ua.dp.dryzhyryk.big.brother.resources.jira.processors.ReportBySprintProcessor;
import ua.dp.dryzhyryk.big.brother.resources.jira.search.PeopleSearchRequest;
import ua.dp.dryzhyryk.big.brother.resources.jira.search.SearchRequests;
import ua.dp.dryzhyryk.big.brother.resources.jira.utils.JsonUtils;

import java.io.File;
import java.util.List;

@Slf4j
public class BigBrotherConsoleApplication {

    private final Configurations config;
    private final ReportBySprintProcessor reportBySprintProcessor;
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
        JiraDataStorage jiraDataStorage = new JiraFileDataStorage(storageRoot.getAbsolutePath());
        JiraInformationHolder jiraInformationHolder = newJiraInformationHolder(jiraResource, jiraDataStorage, config);
        TasksTreeViewMetricsCalculator tasksTreeViewMetricsCalculator = new TasksTreeViewMetricsCalculator();
        TasksRootViewMetricsCalculator tasksRootViewMetricsCalculator = new TasksRootViewMetricsCalculator();
        PeopleViewMetricsCalculatorOld peopleViewMetricsCalculator = new PeopleViewMetricsCalculatorOld();
        SprintViewMetricsCalculator sprintViewMetricsCalculator = new SprintViewMetricsCalculator();
        BigJiraBrother bigJiraBrother = new BigJiraBrother(jiraInformationHolder, tasksTreeViewMetricsCalculator, tasksRootViewMetricsCalculator,
                sprintViewMetricsCalculator);

        DateTimeProvider dateTimeProvider = newDateTimeProvider();

        BigJiraBrotherPeopleViewProviderOld bigJiraBrotherPeopleViewProvider = new BigJiraBrotherPeopleViewProviderOld(jiraInformationHolder, peopleViewMetricsCalculator);

        ReportByPersonValidator reportByPersonValidator = new ReportByPersonValidator();

        ReportGenerator reportGenerator = newExcelReportGenerator(reportRoot.getAbsolutePath(), reportByPersonValidator);


        reportBySprintProcessor = new ReportBySprintProcessor(bigJiraBrother, reportGenerator);
        reportByPersonProcessor = new ReportByPersonProcessor(
                bigJiraBrotherPeopleViewProvider,
                reportGenerator,
                dateTimeProvider);
    }

    protected DateTimeProvider newDateTimeProvider() {
        return new DateTimeProvider();
    }

    protected JiraInformationHolder newJiraInformationHolder(JiraResource jiraResource, JiraDataStorage jiraDataStorage,
                                                             Configurations config) {
        JiraInformationHolder jiraInformationHolder = new JiraInformationHolderImpl(jiraResource, jiraDataStorage);
        return config.isDebugEnabled() ? new LogProxy(jiraInformationHolder) : jiraInformationHolder;
    }

    protected ReportGenerator newExcelReportGenerator(String absolutePath, ReportByPersonValidator reportByPersonValidator) {
        return new ExcelReportGenerator(absolutePath, reportByPersonValidator);
    }

    public static void main(String[] args) {

        Configurations config = Configurations.loadFromAppArguments(args);

        BigBrotherConsoleApplication app = new BigBrotherConsoleApplication(config);

        SearchRequests searchRequests = app.loadSearchRequests();

//		reportBySprintProcessor.prepareReportBySprint(searchRequests.getSprintSearchConditions());
//      reportByPersonProcessor.prepareReportByPerson(searchRequests.getPeopleSearchConditions());

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
