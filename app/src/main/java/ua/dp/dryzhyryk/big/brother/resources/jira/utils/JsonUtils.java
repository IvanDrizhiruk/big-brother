package ua.dp.dryzhyryk.big.brother.resources.jira.utils;

import java.io.FileReader;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonUtils {

	public static final Gson GSON = (new GsonBuilder()).create();

	public static <T> T loadJson(String searchFilePath, Class<T> clazz) {
		try (FileReader fileReader = new FileReader(searchFilePath)) {
			return fromJson(clazz, fileReader);
		}
		catch (IOException e) {
			throw new RuntimeException("Unable to parse " + searchFilePath, e);
		}
	}

	private static <T> T fromJson(Class<T> clazz, FileReader fileReader) {
		return GSON.fromJson(fileReader, clazz);
	}

	public static <T> String toJson(T object) {
		return GSON.toJson(object);
	}
}
