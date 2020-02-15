package ua.dp.dryzhyryk.big.brother.resources.jira.processors;

import lombok.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.dp.dryzhyryk.big.brother.core.BigJiraBrotherPeopleViewProvider;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.PeopleSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.utils.DateTomeProvider;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.ExcelReportGenerator;
import ua.dp.dryzhyryk.big.brother.resources.jira.search.PeopleSearchRequest;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportByPersonProcessorTest {

    @Mock
    private BigJiraBrotherPeopleViewProvider viewProvider;

    @Mock
    private ExcelReportGenerator reportGenerator;

    @Mock
    private DateTomeProvider dateTomeProvider;

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
        when(dateTomeProvider.nowLocalDate()).thenReturn(nowMonday);

        LocalDate startPeriod = LocalDate.of(2019,12, 30);
        LocalDate endPeriod = LocalDate.of(2020,1, 6);

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
        when(dateTomeProvider.nowLocalDate()).thenReturn(nowMonday);

        LocalDate startPeriod = LocalDate.of(2019,12, 23);
        LocalDate endPeriod = LocalDate.of(2019,12, 30);

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