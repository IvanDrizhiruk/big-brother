package ua.dp.dryzhyryk.big.brother.tests;

import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.steps.Steps;

public class ReportByPersonForLastFinishedWeekSteps extends Steps {

    @When("prepare report for person $person for last finished week executed at date $dateOfExecution")
    public void whenPrepareReportForPersonForLastFinishedWeekRunAtDate(
            @Named("person") String person,
            @Named("dateOfExecution") String dateOfExecution
    ) {
        System.out.println("==========================> person " + person + " dateOfExecution " + dateOfExecution);
    }

    @Then("the byte array that is returned contains the correct number of bytes.")
    public void checkArrSize() {
        System.out.println("==========================> the byte array that is returned contains the correct number of bytes.");
    }

    @Then("PeopleView is: $examplesTable")
    public void thenPeopleViewIs(ExamplesTable examplesTable) {
        System.out.println("==========================> " + examplesTable);
    }
}
