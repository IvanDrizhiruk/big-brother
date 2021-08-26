package ua.dp.dryzhyryk.big.brother.data.extractor.jira.extention;

import java.net.URI;
import java.util.Collection;

import javax.ws.rs.core.UriBuilder;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.jira.rest.client.api.domain.Worklog;
import com.atlassian.jira.rest.client.internal.async.AbstractAsynchronousRestClient;

import io.atlassian.util.concurrent.Promise;

public class WorklogsRestClient extends AbstractAsynchronousRestClient {

	private final WorklogsJsonParser worklogsParser = new WorklogsJsonParser();
	private final URI baseUri;

	public WorklogsRestClient(final URI serverUri, final HttpClient httpClient) {
		super(httpClient);
		this.baseUri = UriBuilder.fromUri(serverUri).path("/rest/api/latest").build();
	}

	public Promise<Collection<Worklog>> getWorkLogs(String issueKey) {
		URI worklogsUri = UriBuilder.fromUri(baseUri)
				.path("issue")
				.path(issueKey)
				.path("worklog").build();

		return getAndParse(worklogsUri, worklogsParser);

	}
}
