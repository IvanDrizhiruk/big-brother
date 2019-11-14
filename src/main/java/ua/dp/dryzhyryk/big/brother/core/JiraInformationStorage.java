package ua.dp.dryzhyryk.big.brother.core;

import java.time.LocalDate;
import java.util.List;

import ua.dp.dryzhyryk.big.brother.core.model.Task;

public interface JiraInformationStorage {
	List<Task> loadDayForProject(String projectKey, LocalDate date);
}
