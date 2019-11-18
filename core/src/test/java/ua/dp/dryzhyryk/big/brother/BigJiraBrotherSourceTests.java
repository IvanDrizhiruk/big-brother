package ua.dp.dryzhyryk.big.brother;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ua.dp.dryzhyryk.big.brother.core.BigJiraBrother;
import ua.dp.dryzhyryk.big.brother.core.data.source.JiraInformationCache;
import ua.dp.dryzhyryk.big.brother.core.data.source.JiraInformationHolder;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.TasksTreeView;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.SprintSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.ports.JiraResource;

import java.util.List;


class BigJiraBrotherSourceTests {

    private JiraResource jiraResource;
    private JiraInformationHolder jiraInformationHolder;

    @BeforeEach
    private void beforeEachTest() {
        JiraResource jiraResource = null; //TODO mock
        JiraInformationCache jiraInformationCache = new JiraInformationCache(jiraResource);
        JiraInformationHolder jiraInformationHolder = new JiraInformationHolder(jiraInformationCache);
    }

    @Disabled("Disabled until not implemented")
    @Test
    void dataShouldBeReceivedFromJiraResourceAndCashed() {
        //given
        BigJiraBrother bigJiraBrother = new BigJiraBrother(jiraInformationHolder);


        SprintSearchConditions searchConditionsFirst = SprintSearchConditions.builder()
                .project("Mega project")
                .sprint("Mega first sprint")
                .build();

        SprintSearchConditions searchConditionsSecond = searchConditionsFirst.toBuilder().build();

        //TODO when(jiraResource.loadProjectSprint).thenReturn();

        //when
        List<TasksTreeView> actualFirst = bigJiraBrother.prepareTaskView(searchConditionsFirst);
        List<TasksTreeView> actualSecond = bigJiraBrother.prepareTaskView(searchConditionsSecond);

        //then
        //verify jiraResource call 1 with searchConditionsFirst
        //compare expected, actualFirst, actualSecond
    }

}
