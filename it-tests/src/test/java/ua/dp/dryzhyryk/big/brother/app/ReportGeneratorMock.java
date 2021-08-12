package ua.dp.dryzhyryk.big.brother.app;

import lombok.Getter;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.PeopleView;
import ua.dp.dryzhyryk.big.brother.report.generator.ReportGeneratorOld;

@Getter
public class ReportGeneratorMock implements ReportGeneratorOld {

    private PeopleView lastGeneratedPeopleView;

    @Override
    public void generatePeopleReport(PeopleView peopleView) {
        this.lastGeneratedPeopleView = peopleView;
    }
}
