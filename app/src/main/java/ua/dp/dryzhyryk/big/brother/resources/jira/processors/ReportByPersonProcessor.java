package ua.dp.dryzhyryk.big.brother.resources.jira.processors;

import lombok.extern.slf4j.Slf4j;
import ua.dp.dryzhyryk.big.brother.core.BigJiraBrotherPeopleViewProvider;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.PeopleSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.PeopleView;
import ua.dp.dryzhyryk.big.brother.core.ports.ReportGenerator;
import ua.dp.dryzhyryk.big.brother.core.utils.DateTimeProvider;
import ua.dp.dryzhyryk.big.brother.resources.jira.search.PeopleSearchRequest;
import ua.dp.dryzhyryk.big.brother.utils.PrintUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

//FIXME move to core
@Slf4j
public class ReportByPersonProcessor {

    private final BigJiraBrotherPeopleViewProvider bigJiraBrotherPeopleViewProvider;
    private final ReportGenerator reportGenerator;
    private final DateTimeProvider dateTimeProvider;

    public ReportByPersonProcessor(BigJiraBrotherPeopleViewProvider bigJiraBrotherPeopleViewProvider, ReportGenerator reportGenerator, DateTimeProvider dateTimeProvider) {
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

        LocalDate mondayOfLastFinishedWeek = dateTimeProvider.nowLocalDate()
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

    public void prepareReportByPerson(List<PeopleSearchRequest> peopleSearchRequest) {
        if (null == peopleSearchRequest) {
            return;
        }

        peopleSearchRequest.stream()
                .flatMap(this::toPeopleSearchConditions)
                .forEach(condition -> {
                    PeopleView peopleView = bigJiraBrotherPeopleViewProvider.preparePeopleView(condition);
                    reportGenerator.generatePeopleReport(peopleView);
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

        long numOfDaysBetween = ChronoUnit.DAYS.between(beginOfTheTime, LocalDate.now()) / periodDurationInDays;
        return IntStream.iterate(0, i -> i + periodDurationInDays)
                .limit(numOfDaysBetween)
                .mapToObj(beginOfTheTime::plusDays)
                .collect(Collectors.toList());
    }
}
