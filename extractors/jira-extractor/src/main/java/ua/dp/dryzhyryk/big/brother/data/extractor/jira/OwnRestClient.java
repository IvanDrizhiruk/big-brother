package ua.dp.dryzhyryk.big.brother.data.extractor.jira;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.jira.rest.client.internal.async.AbstractAsynchronousRestClient;
import com.atlassian.jira.rest.client.internal.json.IssueJsonParser;

import java.net.URI;

public class OwnRestClient extends AbstractAsynchronousRestClient {

    private final IssueJsonParser issueParser = new IssueJsonParser();
    private final HttpClient client;

    public OwnRestClient(URI baseUri, HttpClient client) {
        super(client);
        this.client = client;
    }

    public void loadWorkLog() {
        callAndParse(client.newRequest("").setAccept("application/json").get(), issueParser);
    }
}
