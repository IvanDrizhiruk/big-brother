package ua.dp.dryzhyryk.big.brother.report.generator;


import ua.dp.dryzhyryk.big.brother.core.ports.model.view.people.response.PeopleView;

public interface ReportGenerator {

    void generatePeopleReport(PeopleView peopleView);
}
