package ua.dp.dryzhyryk.big.brother.resources.jira;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import ua.dp.dryzhyryk.big.brother.core.BigJiraBrother;
import ua.dp.dryzhyryk.big.brother.core.data.source.JiraInformationCache;
import ua.dp.dryzhyryk.big.brother.core.data.source.JiraInformationHolder;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.TasksTree;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.SprintSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.ports.JiraResource;
import ua.dp.dryzhyryk.big.brother.data.extractor.jira.JiraDataExtractor;

import java.net.URI;

public class BigBrotherConsoleApplication {

    public static void main(String[] args) {

        URI uri = URI.create("https://jira.dp.ua");
        String username = "mega_user";
        String password = "mega_password";

        AsynchronousJiraRestClientFactory jiraRestClientFactory = new AsynchronousJiraRestClientFactory();
        JiraRestClient jiraRestClient = jiraRestClientFactory.createWithBasicHttpAuthentication(uri, username, password);


        JiraResource jiraResource = new JiraDataExtractor(jiraRestClient);
        JiraInformationCache jiraInformationCache = new JiraInformationCache(jiraResource);
        JiraInformationHolder jiraInformationHolder = new JiraInformationHolder(jiraInformationCache);
        BigJiraBrother bigJiraBrother = new BigJiraBrother(jiraInformationHolder);


        SprintSearchConditions sprintSearchConditions = SprintSearchConditions
                .builder()
                .project("PROJECT_NAME")
                .sprint("SPRINT_NAME")
                .build();


        TasksTree tasksTree = jiraInformationHolder.getTasksAsTree(sprintSearchConditions);

        //		Gson gson = (new GsonBuilder()).create();
        //
        //		System.out.println(gson.toJson(tasksTree));

        tasksTree.getRootTasks().forEach(task -> {
            System.out.println(task.getId() + " " + task.getName());
            System.out.println(
                    "Estimated " + convertMinutesToHour(task.getOriginalEstimateMinutes()) +
                            " Real " + convertMinutesToHour(task.getTimeSpentMinutes()) +
                            " Remaining " + convertMinutesToHour(task.getRemainingEstimateMinutes()));

            task.getWorkLogs().forEach(worklog -> {
                System.out.println(worklog.getPerson() + " " + worklog.getMinutesSpent() + " " + worklog.getStartDateTime());
            });

            task.getSubTasks().forEach(subTask -> {
                System.out.println("          " + subTask.getId() + " " + subTask.getName());
                System.out.println("          " +
                        "Estimated " + subTask.getOriginalEstimateMinutes() +
                        " Real " + subTask.getTimeSpentMinutes() +
                        " Remaining " + subTask.getRemainingEstimateMinutes());

                subTask.getWorkLogs().forEach(worklog -> {
                    System.out.println("          " + worklog.getPerson() + " "
                            + convertMinutesToHour(worklog.getMinutesSpent()) + " "
                            + worklog.getStartDateTime());
                });

                System.out.println("");
            });
            System.out.println("");

        });
    }

    private static Integer convertMinutesToHour(Integer minutes) {
        if (null == minutes || minutes.equals(0)) {
            return 0;
        }
        return minutes / 60;
    }
}
