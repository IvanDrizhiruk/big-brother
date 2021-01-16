package ua.dp.dryzhyryk.big.brother.app;

import lombok.Getter;
import org.apache.commons.lang.NotImplementedException;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.PeopleView;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.SprintView;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.TasksRootView;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.TasksTreeView;
import ua.dp.dryzhyryk.big.brother.core.ports.ReportGenerator;

@Getter
public class ReportGeneratorMock implements ReportGenerator {

    private PeopleView lastGeneratedPeopleView;

    @Override
    public void generateReport(TasksTreeView tasksTreeView, SprintView sprintView, TasksRootView tasksRootView) {
        throw new NotImplementedException();
    }

    @Override
    public void generatePeopleReport(PeopleView peopleView) {
        this.lastGeneratedPeopleView = peopleView;
    }
}
