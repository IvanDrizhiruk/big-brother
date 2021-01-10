package ua.dp.dryzhyryk.big.brother.tests;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.steps.Steps;

public class JiraDataSteps extends Steps {

    @Given("task: $task with work log: $workLog")
    public void setFileName(
            @Named("task") ExamplesTable task,
            @Named("workLog") ExamplesTable workLog) {
        System.out.println("==========================> \n task " + task + " workLog " + workLog);
    }
}
