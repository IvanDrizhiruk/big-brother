package ua.dp.dryzhyryk.big.brother.core.data.source;

import java.util.List;

import ua.dp.dryzhyryk.big.brother.core.data.source.model.Task;
import ua.dp.dryzhyryk.big.brother.core.data.source.model.search.SearchConditions;

public interface IJiraInformationHolder {

	List<Task> getTasks(SearchConditions searchConditions);
}
