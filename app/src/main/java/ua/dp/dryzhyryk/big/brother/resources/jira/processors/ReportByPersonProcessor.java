package ua.dp.dryzhyryk.big.brother.resources.jira.processors;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import ua.dp.dryzhyryk.big.brother.core.BigJiraBrotherPeopleViewProvider;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.request.PeopleSearchConditions;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.request.TasksGroupConditions;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.request.TasksGroupConditions.FieldNameAndValuePair;
import ua.dp.dryzhyryk.big.brother.core.ports.model.view.request.TasksGroupsConditions;
import ua.dp.dryzhyryk.big.brother.core.utils.DateTimeProvider;
import ua.dp.dryzhyryk.big.brother.core.utils.PrintUtils;
import ua.dp.dryzhyryk.big.brother.report.generator.excel.ExcelReportGenerator;
import ua.dp.dryzhyryk.big.brother.resources.jira.search.PeopleSearchRequest;
import ua.dp.dryzhyryk.big.brother.resources.jira.search.TasksGroupingConditions;
import ua.dp.dryzhyryk.big.brother.resources.jira.search.TasksGroupingConditions.FieldNameAndValue;

//FIXME move to core
@Slf4j
public class ReportByPersonProcessor {

	private final BigJiraBrotherPeopleViewProvider bigJiraBrotherPeopleViewProvider;
	private final ExcelReportGenerator reportGenerator;
	private final DateTimeProvider dateTimeProvider;

	public ReportByPersonProcessor(BigJiraBrotherPeopleViewProvider bigJiraBrotherPeopleViewProvider, ExcelReportGenerator reportGenerator,
			DateTimeProvider dateTimeProvider) {
		this.bigJiraBrotherPeopleViewProvider = bigJiraBrotherPeopleViewProvider;
		this.reportGenerator = reportGenerator;
		this.dateTimeProvider = dateTimeProvider;
	}

	public void prepareReportByPersonForLastFinishedWeek(List<PeopleSearchRequest> peopleSearchRequest) {
		if (null == peopleSearchRequest) {
			return;
		}

		peopleSearchRequest.stream()
				.map(request ->
						bigJiraBrotherPeopleViewProvider.preparePeopleView(
								toPeopleSearchConditionsForLastFinishedWeek(request),
								toTasksGroupsConditions(request)))
				.peek(peopleView -> PrintUtils.printPeopleView(peopleView, log))
				.forEach(reportGenerator::generatePeopleReport);
	}

	private PeopleSearchConditions toPeopleSearchConditionsForLastFinishedWeek(PeopleSearchRequest peopleSearchRequest) {
		LocalDate now = dateTimeProvider.nowLocalDate();
		//        LocalDate now = LocalDate.of(2021, 1, 5);
		LocalDate mondayOfLastFinishedWeek = now
				.with(DayOfWeek.MONDAY)
				.minusWeeks(1);

		LocalDate sundayOfLastFinishedWeek = mondayOfLastFinishedWeek.plusWeeks(1);

		return PeopleSearchConditions.builder()
				.teamName(peopleSearchRequest.getTeamName())
				.peopleNames(peopleSearchRequest.getPeopleNames())
				.startPeriod(mondayOfLastFinishedWeek)
				.endPeriod(sundayOfLastFinishedWeek)
				.build();
	}

	private TasksGroupsConditions toTasksGroupsConditions(PeopleSearchRequest peopleSearchRequest) {

		TasksGroupConditions unFunctionalTasks = toTasksGroupConditions(peopleSearchRequest.getUnFunctionalTasksGroupConditions());
		TasksGroupConditions inProgressTasks = toTasksGroupConditions(peopleSearchRequest.getInProgressTasksGroupConditions());

		return TasksGroupsConditions.builder()
				.unFunctionalTasksGroupConditions(unFunctionalTasks)
				.inProgressTasksGroupConditions(inProgressTasks)
				.build();
	}

	private TasksGroupConditions toTasksGroupConditions(TasksGroupingConditions tasksGroupingConditions) {
		List<FieldNameAndValue> byFields = tasksGroupingConditions.getByFields();
		Set<String> byStatus = tasksGroupingConditions.getByStatus();

		List<FieldNameAndValuePair> byFieldsCondition = Optional.ofNullable(byFields)
				.orElse(Collections.emptyList())
				.stream()
				.map(fieldAndValue -> new FieldNameAndValuePair(fieldAndValue.getName(), fieldAndValue.getValue()))
				.collect(Collectors.toList());

		return TasksGroupConditions.builder()
				.byFields(byFieldsCondition)
				.byStatus(byStatus)
				.build();
	}
}
