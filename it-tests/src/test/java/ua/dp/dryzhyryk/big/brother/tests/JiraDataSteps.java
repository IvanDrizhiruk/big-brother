package ua.dp.dryzhyryk.big.brother.tests;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.steps.Steps;

public class JiraDataSteps extends Steps {

    @Given("a file, $filename")
    public void setFileName(@Named("filename") String filename) {
        System.out.println("==========================> a file, " + filename);
    }

    @When("the caller loads the file as a byte array")
    public void loadFile() {
        System.out.println("==========================> the caller loads the file as a byte array");
    }

    @Then("the byte array that is returned contains the correct number of bytes.")
    public void checkArrSize() {
        System.out.println("==========================> the byte array that is returned contains the correct number of bytes.");
    }
}
