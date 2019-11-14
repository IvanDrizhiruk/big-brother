package ua.dp.dryzhyryk.big.brother.core.ports;

import java.time.LocalDate;
import java.util.List;

import ua.dp.dryzhyryk.big.brother.core.model.Task;

public interface JiraResource {

	List<Task> loadDayForProject(String projectKey, LocalDate date);
}
