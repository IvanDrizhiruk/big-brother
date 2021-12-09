package ua.dp.dryzhyryk.big.brother.resources.jira.processors;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.dp.dryzhyryk.big.brother.core.BigJiraBrotherPeopleViewProviderOld;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.request.PeopleSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.utils.DateTimeProvider;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.ExcelReportGeneratorOld;
import ua.dp.dryzhyryk.big.brother.resources.jira.search.PeopleSearchRequest;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@Disabled
@ExtendWith(MockitoExtension.class)
class ReportByPersonProcessorTest {

    @Mock
    private BigJiraBrotherPeopleViewProviderOld viewProvider;

    @Mock
    private ExcelReportGeneratorOld reportGenerator;

    @Mock
    private DateTimeProvider dateTimeProvider;

    @InjectMocks
    private ReportByPersonProcessor processor;

    @Test
    public void previousWeekShouldBeRequestedOnFirstMondayOfTheWeekAndYear() {
        //given
        String teamName = "Ducks";
        List<String> peopleNames = Arrays.asList("o_dkoval", "o_ssolov", "o_okolom", "o_ystepa", "o_izhytn");

        List<PeopleSearchRequest> peopleSearchRequest = Collections.singletonList(
                PeopleSearchRequest.builder()
                        .teamName(teamName)
                        .peopleNames(peopleNames)
                        .build()
        );

        LocalDate nowMonday = LocalDate.of(2020, 1, 6);
        when(dateTimeProvider.nowLocalDate()).thenReturn(nowMonday);

        LocalDate startPeriod = LocalDate.of(2019, 12, 30);
        LocalDate endPeriod = LocalDate.of(2020, 1, 6);

        PeopleSearchConditions peopleSearchConditions = PeopleSearchConditions.builder()
                .teamName(teamName)
                .peopleNames(peopleNames)
                .startPeriod(startPeriod)
                .endPeriod(endPeriod)
                .build();

        //when
        processor.prepareReportByPersonForLastFinishedWeek(peopleSearchRequest);

        //then
        verify(viewProvider, timeout(1)).preparePeopleView(peopleSearchConditions);
        verify(reportGenerator, times(1)).generatePeopleReport(any());

        Mockito.verifyNoMoreInteractions(viewProvider, reportGenerator);
    }

    @Test
    public void previousWeekShouldBeRequestedOnFirstSundayOfTheWeekAndYear() {
        //given
        String teamName = "Ducks";
        List<String> peopleNames = Arrays.asList("o_dkoval", "o_ssolov", "o_okolom", "o_ystepa", "o_izhytn");

        List<PeopleSearchRequest> peopleSearchRequest = Collections.singletonList(
                PeopleSearchRequest.builder()
                        .teamName(teamName)
                        .peopleNames(peopleNames)
                        .build()
        );

        LocalDate nowMonday = LocalDate.of(2020, 1, 5);
        when(dateTimeProvider.nowLocalDate()).thenReturn(nowMonday);

        LocalDate startPeriod = LocalDate.of(2019, 12, 23);
        LocalDate endPeriod = LocalDate.of(2019, 12, 30);

        PeopleSearchConditions peopleSearchConditions = PeopleSearchConditions.builder()
                .teamName(teamName)
                .peopleNames(peopleNames)
                .startPeriod(startPeriod)
                .endPeriod(endPeriod)
                .build();

        //when
        processor.prepareReportByPersonForLastFinishedWeek(peopleSearchRequest);

        //then
        verify(viewProvider, timeout(1)).preparePeopleView(peopleSearchConditions);
        verify(reportGenerator, times(1)).generatePeopleReport(any());

        Mockito.verifyNoMoreInteractions(viewProvider, reportGenerator);
    }
}