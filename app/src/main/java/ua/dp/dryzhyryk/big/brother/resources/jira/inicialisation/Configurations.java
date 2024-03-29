package ua.dp.dryzhyryk.big.brother.resources.jira.inicialisation;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Value
@Builder
public class Configurations {

	private final String rootDir;
	private final URI jiraUri;
	private final String jiraUsername;
	private final String jiraPassword;
	private final boolean isDebugEnabled;
	private final List<String> fieldNamesForLoading;

	public Configurations(String rootDir, URI jiraUri, String jiraUsername, String jiraPassword, boolean isDebugEnabled,
			List<String> fieldNamesForLoading) {
		this.rootDir = rootDir;
		this.jiraUri = jiraUri;
		this.jiraUsername = jiraUsername;
		this.jiraPassword = jiraPassword;
		this.isDebugEnabled = isDebugEnabled;
		this.fieldNamesForLoading = fieldNamesForLoading;
	}

	public static Configurations loadFromAppArguments(String[] args) {
		String rootDir = extract("rootDir", args).orElse("./");

		Properties jiraProperties = loadProperties(rootDir + "config/jira.properties");

		URI jiraUri = URI.create(jiraProperties.getProperty("jira.url"));
		String jiraUsername = jiraProperties.getProperty("jira.user");
		String jiraPassword = jiraProperties.getProperty("jira.password");

		List<String> fieldNamesForLoading = Stream.of(jiraProperties.getProperty("jira.task.fields.name.for.loading").split(","))
				.collect(Collectors.toList());

		Properties configProperties = loadProperties(rootDir + "config/config.properties");
		boolean isDebugEnabled = Boolean.parseBoolean(configProperties.getProperty("isDebugEnabled", "true"));

		return new Configurations(rootDir, jiraUri, jiraUsername, jiraPassword, isDebugEnabled, fieldNamesForLoading);
	}

	private static Optional<String> extract(String parameterName, String[] args) {
		return Stream.of(args)
				.filter(arg -> arg.startsWith(parameterName))
				.map(arg -> arg.substring(parameterName.length() + 1))
				.findFirst();
	}

	private static Properties loadProperties(String fileName) {
		try (InputStream input = new FileInputStream(fileName)) {

			Properties prop = new Properties();
			prop.load(input);

			return prop;

		}
		catch (IOException e) {
			throw new IllegalArgumentException("Sorry, unable to find " + fileName, e);
		}
	}

	public List<String> getFieldNamesForLoading() {
		return fieldNamesForLoading;
	}
}

