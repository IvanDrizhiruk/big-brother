package ua.dp.dryzhyryk.big.brother.data.extractor.jira.extention;

import java.io.IOException;
import java.net.URI;

import com.atlassian.jira.rest.client.api.AuditRestClient;
import com.atlassian.jira.rest.client.api.ComponentRestClient;
import com.atlassian.jira.rest.client.api.GroupRestClient;
import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.MetadataRestClient;
import com.atlassian.jira.rest.client.api.MyPermissionsRestClient;
import com.atlassian.jira.rest.client.api.ProjectRestClient;
import com.atlassian.jira.rest.client.api.ProjectRolesRestClient;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.SessionRestClient;
import com.atlassian.jira.rest.client.api.UserRestClient;
import com.atlassian.jira.rest.client.api.VersionRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClient;
import com.atlassian.jira.rest.client.internal.async.DisposableHttpClient;

public class JiraRestClientExtendedImpl implements JiraRestClientExtended {

	private final JiraRestClient jiraRestClient;
	private final WorklogsRestClient worklogsRestClient;

	public JiraRestClientExtendedImpl(final URI serverUri, final DisposableHttpClient httpClient) {
		jiraRestClient = new AsynchronousJiraRestClient(serverUri, httpClient);
		worklogsRestClient = new WorklogsRestClient(serverUri, httpClient);
	}

	@Override
	public IssueRestClient getIssueClient() {
		return jiraRestClient.getIssueClient();
	}

	@Override
	public SessionRestClient getSessionClient() {
		return jiraRestClient.getSessionClient();
	}

	@Override
	public UserRestClient getUserClient() {
		return jiraRestClient.getUserClient();
	}

	@Override
	public GroupRestClient getGroupClient() {
		return jiraRestClient.getGroupClient();
	}

	@Override
	public ProjectRestClient getProjectClient() {
		return jiraRestClient.getProjectClient();
	}

	@Override
	public ComponentRestClient getComponentClient() {
		return jiraRestClient.getComponentClient();
	}

	@Override
	public MetadataRestClient getMetadataClient() {
		return jiraRestClient.getMetadataClient();
	}

	@Override
	public SearchRestClient getSearchClient() {
		return jiraRestClient.getSearchClient();
	}

	@Override
	public VersionRestClient getVersionRestClient() {
		return jiraRestClient.getVersionRestClient();
	}

	@Override
	public ProjectRolesRestClient getProjectRolesRestClient() {
		return jiraRestClient.getProjectRolesRestClient();
	}

	@Override
	public AuditRestClient getAuditRestClient() {
		return jiraRestClient.getAuditRestClient();
	}

	@Override
	public MyPermissionsRestClient getMyPermissionsRestClient() {
		return jiraRestClient.getMyPermissionsRestClient();
	}

	@Override
	public void close() throws IOException {
		jiraRestClient.close();
	}

	@Override
	public WorklogsRestClient getWorklogsClient() {
		return worklogsRestClient;
	}
}
