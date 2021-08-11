package ua.dp.dryzhyryk.big.brother.core.ports;

import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.PeopleView;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.SprintView;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.TasksRootView;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.TasksTreeView;

public interface ReportGenerator {

    void generatePeopleReport(PeopleView peopleView);
}
