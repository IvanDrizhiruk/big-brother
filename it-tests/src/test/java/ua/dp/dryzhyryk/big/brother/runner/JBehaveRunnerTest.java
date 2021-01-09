package ua.dp.dryzhyryk.big.brother.runner;


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
import ua.dp.dryzhyryk.big.brother.resources.jira.BigBrotherConsoleApplication;
import ua.dp.dryzhyryk.big.brother.resources.jira.inicialisation.Configurations;
import ua.dp.dryzhyryk.big.brother.tests.JiraDataSteps;
import ua.dp.dryzhyryk.big.brother.tests.ReportByPersonForLastFinishedWeekSteps;

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

    @Override
    public InjectableStepsFactory stepsFactory() {

        String[] args = {};
        Configurations config = Configurations.loadFromAppArguments(args);

        BigBrotherConsoleApplication app = new BigBrotherConsoleApplication(config);


        List<Steps> stepFileList = Arrays.asList(
                new JiraDataSteps(),
                new ReportByPersonForLastFinishedWeekSteps()
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
