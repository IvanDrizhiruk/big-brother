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
import ua.dp.dryzhyryk.big.brother.core.data.source.model.Task;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.PersonSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.SearchConditions;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.SprintSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.ports.JiraDataStorage;

@Slf4j
public class JiraFileDataStorage implements JiraDataStorage {

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
	public void saveProjectSprint(SearchConditions searchConditions, List<Task> tasks) {
		String filename = toFileName(searchConditions);
		File fileForStoring = new File(rootStorageDirectory, filename);

		try (FileWriter writer = new FileWriter(fileForStoring)) {

			gson.toJson(tasks, writer);
		}
		catch (IOException e) {
			log.error("Unable to store data for " + searchConditions.toString(), e);
		}
	}

	@Override
	public List<Task> loadTasks(SearchConditions searchConditions) {
		String filename = toFileName(searchConditions);
		File fileForStoring = new File(rootStorageDirectory, filename);

		if (!fileForStoring.isFile() || !fileForStoring.exists()) {
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

	private String toFileName(SearchConditions searchConditions) {
		switch (searchConditions.getSearchConditionType()) {
			case SPRINT:
				return toFileName((SprintSearchConditions) searchConditions);
			case PERSON:
				return toFileName((PersonSearchConditions) searchConditions);
		}

		throw new IllegalArgumentException(
				"Unable to prepare filename. Unsupported search type " + searchConditions.getSearchConditionType());
	}

	private String toFileName(SprintSearchConditions searchConditions) {
		return String.format("[%s] [%s].json", searchConditions.getProject(), searchConditions.getSprint());
	}

	private String toFileName(PersonSearchConditions searchConditions) {
		return String.format("[%s] %s-%s.json", searchConditions.getPersonName(),
				searchConditions.getStartPeriod().format(DateTimeFormatter.ISO_LOCAL_DATE),
				searchConditions.getStartPeriod().format(DateTimeFormatter.ISO_LOCAL_DATE));
	}
}
