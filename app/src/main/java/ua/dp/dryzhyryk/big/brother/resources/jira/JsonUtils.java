package ua.dp.dryzhyryk.big.brother.resources.jira;

import java.io.FileReader;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonUtils {

	public static <T> T loadJson(String serchFilePath, Class<T> clazz) {
		Gson gson = (new GsonBuilder()).create();
		try (FileReader fileReader = new FileReader(serchFilePath)) {
			return gson.fromJson(fileReader, clazz);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
