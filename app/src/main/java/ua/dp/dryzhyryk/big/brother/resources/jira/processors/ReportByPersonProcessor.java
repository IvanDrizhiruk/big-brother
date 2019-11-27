package ua.dp.dryzhyryk.big.brother.resources.jira.processors;

import ua.dp.dryzhyryk.big.brother.core.BigJiraBrotherPeopleView;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.PeopleSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.PeopleView;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.ExcelReportGenerator;
import ua.dp.dryzhyryk.big.brother.resources.jira.search.PeopleSearchRequest;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ReportByPersonProcessor {

    private final BigJiraBrotherPeopleView bigJiraBrotherPeopleView;
    private final ExcelReportGenerator reportGenerator;

    public ReportByPersonProcessor(BigJiraBrotherPeopleView bigJiraBrotherPeopleView, ExcelReportGenerator reportGenerator) {
        this.bigJiraBrotherPeopleView = bigJiraBrotherPeopleView;
        this.reportGenerator = reportGenerator;
    }

    public void prepareReportByPerson(List<PeopleSearchRequest> peopleSearchRequest) {
        if (null == peopleSearchRequest) {
            return;
        }

        peopleSearchRequest.stream()
                .flatMap(this::toPeopleSearchConditions)
                .forEach(condition -> {
                    PeopleView peopleView = bigJiraBrotherPeopleView.preparePeopleView(condition);
                    reportGenerator.generateReport(peopleView);
                });
    }

    private Stream<PeopleSearchConditions> toPeopleSearchConditions(PeopleSearchRequest peopleSearchRequest) {

        LocalDate beginOfTheTime = peopleSearchRequest.getBeginOfTheTime();
        int periodDurationInDays = peopleSearchRequest.getPeriodDurationInDays();

        return getDatesToNow(beginOfTheTime, periodDurationInDays).stream()
                .map(startDate -> PeopleSearchConditions.builder()
                        .teamName(peopleSearchRequest.getTeamName())
                        .peopleNames(peopleSearchRequest.getPeopleNames())
                        .startPeriod(startDate)
                        .endPeriod(startDate.plusDays(periodDurationInDays))
                        .build());
    }

    private static List<LocalDate> getDatesToNow(LocalDate beginOfTheTime, int periodDurationInDays) {

        long numOfDaysBetween = ChronoUnit.DAYS.between(beginOfTheTime, LocalDate.now());
        return IntStream.iterate(0, i -> i + periodDurationInDays)
                .limit(numOfDaysBetween)
                .mapToObj(beginOfTheTime::plusDays)
                .collect(Collectors.toList());
    }
}
