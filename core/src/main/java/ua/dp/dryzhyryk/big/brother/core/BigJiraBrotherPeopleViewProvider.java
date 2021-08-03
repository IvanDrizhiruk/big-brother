package ua.dp.dryzhyryk.big.brother.core;

import lombok.extern.slf4j.Slf4j;
import ua.dp.dryzhyryk.big.brother.core.data.source.IJiraInformationHolder;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.Task;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.PeopleSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.PersonSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.person.PeopleViewMetricsCalculatorOld;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.PeopleView;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.person.model.PersonMetrics;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class BigJiraBrotherPeopleViewProvider {

    private final IJiraInformationHolder jiraInformationHolder;
    private final PeopleViewMetricsCalculatorOld peopleViewMetricsCalculator;

    public BigJiraBrotherPeopleViewProvider(
            IJiraInformationHolder jiraInformationHolder,
            PeopleViewMetricsCalculatorOld peopleViewMetricsCalculator) {
        this.jiraInformationHolder = jiraInformationHolder;
        this.peopleViewMetricsCalculator = peopleViewMetricsCalculator;
    }


    public PeopleView preparePeopleView(PeopleSearchConditions peopleSearchConditions) {

        List<Task> tasks = peopleSearchConditions.getPeopleNames().stream()
                .map(personName -> PersonSearchConditions.builder()
                        .personName(personName)
                        .startPeriod(peopleSearchConditions.getStartPeriod())
                        .endPeriod(peopleSearchConditions.getEndPeriod())
                        .build())
                .map(jiraInformationHolder::getTasks)
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());

        List<PersonMetrics> personMetrics = peopleViewMetricsCalculator.calculateFor(tasks, peopleSearchConditions);

        return PeopleView.builder()
                .teamName(peopleSearchConditions.getTeamName())
                .startPeriod(peopleSearchConditions.getStartPeriod())
                .endPeriod(peopleSearchConditions.getEndPeriod())
                .personMetrics(personMetrics)
                .build();
    }
}
