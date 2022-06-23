package ua.dp.dryzhyryk.big.brother.data.extractor.jira;

import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class JiraExtraConfiguration {

	List<String> fieldNamesForLoading;

}
