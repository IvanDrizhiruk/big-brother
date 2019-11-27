package ua.dp.dryzhyryk.big.brother.resources.jira.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileReader;
import java.io.IOException;

public class JsonUtils {

    public static final Gson GSON = (new GsonBuilder()).create();

    public static <T> T loadJson(String serchFilePath, Class<T> clazz) {
        try (FileReader fileReader = new FileReader(serchFilePath)) {
            return fromJson(clazz, fileReader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T fromJson(Class<T> clazz, FileReader fileReader) {
        return GSON.fromJson(fileReader, clazz);
    }

    public static <T> String toJson(T object) {
        return GSON.toJson(object);
    }
}
