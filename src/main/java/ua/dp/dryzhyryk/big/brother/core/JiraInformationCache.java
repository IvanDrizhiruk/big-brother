package ua.dp.dryzhyryk.big.brother.core;

import org.springframework.stereotype.Service;
import ua.dp.dryzhyryk.big.brother.core.model.Task;
import ua.dp.dryzhyryk.big.brother.core.ports.JiraResource;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class JiraInformationCache {

    private final JiraResource jiraResource;
    private final JiraInformationStorage jiraInformationStorage;

    private final Map<String, Map<LocalDate, List<Task>>> tasksByProjectKeyAndDate = new HashMap<>();

    public JiraInformationCache(JiraResource jiraResource, JiraInformationStorage jiraInformationStorage) {
        this.jiraResource = jiraResource;
        this.jiraInformationStorage = jiraInformationStorage;
    }

    public List<Task> getRootTasks(String projectKey, LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, List<Task>> tasksForProject =
                tasksByProjectKeyAndDate.computeIfAbsent(projectKey, __ -> loadProjectData(projectKey, startDate, endDate));

        return createStreamFor(startDate, endDate)
                .map(date -> tasksForProject.computeIfAbsent(date, key -> loadProjectData(projectKey, date)))
				.flatMap(List::stream)
                .collect(Collectors.toList());

    }

    private Stream<LocalDate> createStreamFor(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date can not be after end date");
        }

        long daysFromStartToEnd = endDate.toEpochDay() - startDate.toEpochDay();
        return Stream.iterate(startDate, date -> date.plusDays(1))
                .limit(daysFromStartToEnd);
    }

    private Map<LocalDate, List<Task>> loadProjectData(String projectKey, LocalDate startDate, LocalDate endDate) {
        return createStreamFor(startDate, endDate)
                .collect(
                        Collectors.toMap(
                                Function.identity(),
                                currentDate -> loadProjectData(projectKey, currentDate)));
    }

    private List<Task> loadProjectData(String projectKey, LocalDate date) {
        List<Task> loadedDataFromStorage = jiraInformationStorage.loadDayForProject(projectKey, date);

        if (null != loadedDataFromStorage) {
            return loadedDataFromStorage;
        }

        return jiraResource.loadDayForProject(projectKey, date);
    }
}
