package ua.dp.dryzhyryk.big.brother.runner;


import lombok.SneakyThrows;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.Steps;
import org.mockito.Mockito;
import ua.dp.dryzhyryk.big.brother.app.ReportGeneratorMock;
import ua.dp.dryzhyryk.big.brother.core.data.source.JiraInformationHolderImpl;
import ua.dp.dryzhyryk.big.brother.core.ports.JiraDataStorage;
import ua.dp.dryzhyryk.big.brother.core.ports.JiraResource;
import ua.dp.dryzhyryk.big.brother.core.utils.DateTimeProvider;
import ua.dp.dryzhyryk.big.brother.resources.jira.BigBrotherConsoleApplication;
import ua.dp.dryzhyryk.big.brother.resources.jira.inicialisation.Configurations;
import ua.dp.dryzhyryk.big.brother.tests.JiraInformationHolderMockingSteps;
import ua.dp.dryzhyryk.big.brother.tests.ReportByPersonForLastFinishedWeekSteps;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JBehaveRunnerTest extends JUnitStories {

    @Override
    public Configuration configuration() {
        return new MostUsefulConfiguration()
                .useStoryLoader(
                        new LoadFromClasspath(this.getClass().getClassLoader()))
                .useStoryReporterBuilder(
                        new StoryReporterBuilder()
                                .withDefaultFormats()
                                .withFormats(Format.HTML, Format.CONSOLE)
                                .withRelativeDirectory("jbehave-report")
                );
    }

    @SneakyThrows
    @Override
    public InjectableStepsFactory stepsFactory() {
        Configurations config = Configurations.builder()
                .rootDir("target/")
                .jiraUri(new URI("mega_url"))
                .jiraUsername("mega_user")
                .jiraPassword("mega_password")
                .build();

        DateTimeProvider dateTimeProviderMock = Mockito.mock(DateTimeProvider.class);
        JiraInformationHolderImpl jiraInformationHolderMock = Mockito.mock(JiraInformationHolderImpl.class);
        ReportGeneratorMock reportGeneratorMock = new ReportGeneratorMock();

        BigBrotherConsoleApplication app = new BigBrotherConsoleApplication(config) {

            @Override
            protected DateTimeProvider newDateTimeProvider() {
                return dateTimeProviderMock;
            }

            @Override
            protected JiraInformationHolderImpl newJiraInformationHolder(JiraResource jiraResource, JiraDataStorage jiraDataStorage,
                                                                         Configurations config) {
                return jiraInformationHolderMock;
            }
        };

        List<Steps> stepFileList = Arrays.asList(
                new JiraInformationHolderMockingSteps(jiraInformationHolderMock),
                new ReportByPersonForLastFinishedWeekSteps(app, dateTimeProviderMock, reportGeneratorMock)
        );

        return new InstanceStepsFactory(configuration(), stepFileList);
    }

    @Override
    protected List<String> storyPaths() {
        return new StoryFinder().
                findPaths(CodeLocations.codeLocationFromClass(
                                this.getClass()),
                        Collections.singletonList("**/*.story"),
                        Collections.singletonList(""));
    }
}
