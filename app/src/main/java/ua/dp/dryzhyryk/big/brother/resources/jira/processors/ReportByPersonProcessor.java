package ua.dp.dryzhyryk.big.brother.resources.jira.processors;

import lombok.extern.slf4j.Slf4j;
import ua.dp.dryzhyryk.big.brother.core.BigJiraBrotherPeopleViewProvider;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.request.PeopleSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.utils.DateTimeProvider;
import ua.dp.dryzhyryk.big.brother.core.utils.PrintUtils;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.ExcelReportGenerator;
import ua.dp.dryzhyryk.big.brother.resources.jira.search.PeopleSearchRequest;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

//FIXME move to core
@Slf4j
public class ReportByPersonProcessor {

    private final BigJiraBrotherPeopleViewProvider bigJiraBrotherPeopleViewProvider;
    private final ExcelReportGenerator reportGenerator;
    private final DateTimeProvider dateTimeProvider;

    public ReportByPersonProcessor(BigJiraBrotherPeopleViewProvider bigJiraBrotherPeopleViewProvider, ExcelReportGenerator reportGenerator, DateTimeProvider dateTimeProvider) {
        this.bigJiraBrotherPeopleViewProvider = bigJiraBrotherPeopleViewProvider;
        this.reportGenerator = reportGenerator;
        this.dateTimeProvider = dateTimeProvider;
    }

    public void prepareReportByPersonForLastFinishedWeek(List<PeopleSearchRequest> peopleSearchRequest) {
        if (null == peopleSearchRequest) {
            return;
        }

        peopleSearchRequest.stream()
                .map(this::toPeopleSearchConditionsForLastFinishedWeek)
                .map(bigJiraBrotherPeopleViewProvider::preparePeopleView)
                .peek(peopleView -> PrintUtils.printPeopleView(peopleView, log))
                .forEach(reportGenerator::generatePeopleReport);
    }

    private PeopleSearchConditions toPeopleSearchConditionsForLastFinishedWeek(PeopleSearchRequest peopleSearchRequest) {
//        LocalDate now = dateTimeProvider.nowLocalDate();
        LocalDate now = LocalDate.of(2021, 1, 5);
        LocalDate mondayOfLastFinishedWeek = now
                .with(DayOfWeek.MONDAY)
                .minusWeeks(1);

        LocalDate sundayOfLastFinishedWeek = mondayOfLastFinishedWeek.plusWeeks(1);

        return PeopleSearchConditions.builder()
                .teamName(peopleSearchRequest.getTeamName())
                .peopleNames(peopleSearchRequest.getPeopleNames())
                .startPeriod(mondayOfLastFinishedWeek)
                .endPeriod(sundayOfLastFinishedWeek)
                .build();
    }
}
