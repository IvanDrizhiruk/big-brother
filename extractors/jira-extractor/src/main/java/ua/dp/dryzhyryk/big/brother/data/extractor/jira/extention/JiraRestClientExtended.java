package ua.dp.dryzhyryk.big.brother.data.extractor.jira.extention;

import com.atlassian.jira.rest.client.api.JiraRestClient;

public interface JiraRestClientExtended extends JiraRestClient {

	WorklogsRestClient getWorklogsClient();
}
