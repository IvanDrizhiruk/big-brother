package ua.dp.dryzhyryk.big.brother.core;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ua.dp.dryzhyryk.big.brother.core.data.source.JiraInformationHolderImpl;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.model.PeopleView;
import ua.dp.dryzhyryk.big.brother.core.metrics.calculator.person.PeopleViewMetricsCalculatorOld;
import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.data.Task;
import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.search.conditions.JiraSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.search.conditions.types.JiraPersonSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.request.PeopleSearchConditions;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BigJiraBrotherPeopleViewProviderTest {

    @Test
    public void loadOfTasksShouldBeCalledForEachPeople() {
        //given

        String firstPerson = "o_dkoval";
        String secondPerson = "o_ssolov";
        String thirdPerson = "o_ystepa";

        LocalDate startPeriod = LocalDate.of(2020, 1, 1);
        LocalDate endPeriod = LocalDate.of(2020, 1, 8);

        PeopleSearchConditions peopleSearchConditions = PeopleSearchConditions.builder()
                .teamName("Ducks")
                .peopleNames(Arrays.asList(firstPerson, secondPerson, thirdPerson))
                .startPeriod(startPeriod)
                .endPeriod(endPeriod)
                .build();

        JiraInformationHolderImpl jiraInformationHolder = mock(JiraInformationHolderImpl.class);
        when(jiraInformationHolder.getTasks(any(JiraSearchConditions.class)))
                .thenReturn(new ArrayList<>());

        JiraPersonSearchConditions firstExpectedSearchCondition = JiraPersonSearchConditions.builder()
                .personName(firstPerson)
                .startPeriod(startPeriod)
                .endPeriod(endPeriod)
                .build();

        JiraPersonSearchConditions secondExpectedSearchCondition = JiraPersonSearchConditions.builder()
                .personName(secondPerson)
                .startPeriod(startPeriod)
                .endPeriod(endPeriod)
                .build();

        JiraPersonSearchConditions thirdExpectedSearchCondition = JiraPersonSearchConditions.builder()
                .personName(thirdPerson)
                .startPeriod(startPeriod)
                .endPeriod(endPeriod)
                .build();

        //when
        BigJiraBrotherPeopleViewProviderOld bigJiraBrotherPeopleViewProvider = new BigJiraBrotherPeopleViewProviderOld(
                jiraInformationHolder, new PeopleViewMetricsCalculatorOld());

        bigJiraBrotherPeopleViewProvider.preparePeopleView(peopleSearchConditions);

        //then
        verify(jiraInformationHolder, times(1)).getTasks(firstExpectedSearchCondition);
        verify(jiraInformationHolder, times(1)).getTasks(secondExpectedSearchCondition);
        verify(jiraInformationHolder, times(1)).getTasks(thirdExpectedSearchCondition);

        Mockito.verifyNoMoreInteractions(jiraInformationHolder);
    }

    @Disabled
    @Test
    public void calculatefor() {
        //given

        PeopleSearchConditions peopleSearchConditions = PeopleSearchConditions.builder().build();

        //TODO
        List<Task> tasks = new ArrayList<>();


        JiraInformationHolderImpl jiraInformationHolder = mock(JiraInformationHolderImpl.class);
//        Mockito.when(jiraInformationHolder.getTasks()).then(tasks);


        //when
        BigJiraBrotherPeopleViewProviderOld bigJiraBrotherPeopleViewProvider = new BigJiraBrotherPeopleViewProviderOld(
                jiraInformationHolder, new PeopleViewMetricsCalculatorOld());

        PeopleView actual = bigJiraBrotherPeopleViewProvider.preparePeopleView(peopleSearchConditions);
        //then

        //TODO expected
    }
}