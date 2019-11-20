package ua.dp.dryzhyryk.big.brother.data.storage.jira;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import lombok.extern.slf4j.Slf4j;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.Task;
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
	public void saveProjectSprint(SprintSearchConditions sprintSearchConditions, List<Task> tasks) {
		String filename = toFileName(sprintSearchConditions);
		File fileForStoring = new File(rootStorageDirectory, filename);

		try (FileWriter writer = new FileWriter(fileForStoring)) {

			gson.toJson(tasks, writer);
		}
		catch (IOException e) {
			log.error("Unable to store data for " + sprintSearchConditions.getProject() + " " + sprintSearchConditions.getSprint(), e);
		}
	}

	@Override
	public List<Task> loadProjectSprint(SprintSearchConditions sprintSearchConditions) {
		String filename = toFileName(sprintSearchConditions);
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
			String errorMessage = "Unable to load data for " + sprintSearchConditions.getProject() + " " + sprintSearchConditions.getSprint();
			log.error(errorMessage, e);

			throw new IllegalArgumentException(errorMessage, e);
		}
	}

	private String toFileName(SprintSearchConditions sprintSearchConditions) {
		return String.format("[%s] [%s].json", sprintSearchConditions.getProject(), sprintSearchConditions.getSprint());
	}
}
