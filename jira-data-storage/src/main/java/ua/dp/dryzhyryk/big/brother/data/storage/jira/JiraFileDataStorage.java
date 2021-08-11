package ua.dp.dryzhyryk.big.brother.data.storage.jira;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import lombok.extern.slf4j.Slf4j;
import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.data.Task;
import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.search.conditions.types.JiraPersonSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.ports.model.jira.search.conditions.JiraSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.ports.DataStorage;

@Slf4j
public class JiraFileDataStorage implements DataStorage {

	private final File rootStorageDirectory;
	private final Gson gson;

	public JiraFileDataStorage(String rootStorageDirectoryPath) {
		this.rootStorageDirectory = new File(rootStorageDirectoryPath);

		if (!rootStorageDirectory.isDirectory()) {
			throw new IllegalArgumentException("Wrong path for root storage directory: " + rootStorageDirectoryPath);
		}

		this.gson = (new GsonBuilder()).create();
	}

	@Override
	public void saveProjectSprint(JiraSearchConditions searchConditions, List<Task> tasks) {
		String filename = toFileName(searchConditions);
		File fileForStoring = new File(rootStorageDirectory, filename);

		try (FileWriter writer = new FileWriter(fileForStoring)) {

			gson.toJson(tasks, writer);
		}
		catch (IOException e) {
			log.error("Unable to store data for " + searchConditions, e);
		}
	}

	@Override
	public List<Task> loadTasks(JiraSearchConditions searchConditions) {
		String filename = toFileName(searchConditions);
		File fileForStoring = new File(rootStorageDirectory, filename);

		if (!fileForStoring.isFile() || !fileForStoring.exists()) {
			log.info("Unable find cashed data in file: {}", fileForStoring.getAbsolutePath());

			return null;
		}

		try (FileReader reader = new FileReader(fileForStoring)) {
			Type type = new TypeToken<List<Task>>() {
			}.getType();

			return gson.fromJson(reader, type);
		}
		catch (IOException e) {
			throw new IllegalArgumentException("Unable to load data for " + searchConditions.toString(), e);
		}
	}

	private String toFileName(JiraSearchConditions searchConditions) {
		switch (searchConditions.getSearchConditionType()) {
			case PERSON:
				return toFileName((JiraPersonSearchConditions) searchConditions);
		}

		throw new IllegalArgumentException(
				"Unable to prepare filename. Unsupported search type " + searchConditions.getSearchConditionType());
	}

	private String toFileName(JiraPersonSearchConditions searchConditions) {
		return String.format("[%s] %s-%s.json", searchConditions.getPersonName(),
				searchConditions.getStartPeriod().format(DateTimeFormatter.ISO_LOCAL_DATE),
				searchConditions.getEndPeriod().format(DateTimeFormatter.ISO_LOCAL_DATE));
	}
}
