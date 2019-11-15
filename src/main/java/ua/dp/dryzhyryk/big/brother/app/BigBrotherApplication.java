package ua.dp.dryzhyryk.big.brother.app;

import java.net.URI;

import javax.annotation.PostConstruct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

import ua.dp.dryzhyryk.big.brother.core.BigJiraBrother;

@SpringBootApplication
@Configuration
@ComponentScan(basePackages = "ua.dp.dryzhyryk.big.brother")
public class BigBrotherApplication {

	private static ConfigurableApplicationContext context;

	public static void main(String[] args) {
		context = SpringApplication.run(BigBrotherApplication.class, args);
		BigJiraBrother bigJiraBrother = context.getBean(BigJiraBrother.class);
	}

	@Bean
	public JiraRestClient newJiraRestClient() {
		URI uri = URI.create("https://jira.dp.ua");
		String username = "mega_user";
		String password = "mega_password";

		AsynchronousJiraRestClientFactory jiraRestClientFactory = new AsynchronousJiraRestClientFactory();
		JiraRestClient jiraRestClient = jiraRestClientFactory.createWithBasicHttpAuthentication(uri, username, password);
		return jiraRestClient;
	}

}
