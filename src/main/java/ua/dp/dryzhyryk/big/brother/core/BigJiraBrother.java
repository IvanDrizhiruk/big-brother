package ua.dp.dryzhyryk.big.brother.core;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ua.dp.dryzhyryk.big.brother.core.model.PeopleView;
import ua.dp.dryzhyryk.big.brother.core.model.TasksTree;
import ua.dp.dryzhyryk.big.brother.core.model.TasksTreeView;
import ua.dp.dryzhyryk.big.brother.core.model.search.SprintSearchConditions;

@Service
public class BigJiraBrother {

	private final JiraInformationHolder jiraInformationHolder;

	@Autowired
	public BigJiraBrother(JiraInformationHolder jiraInformationHolder) {
		this.jiraInformationHolder = jiraInformationHolder;
	}

	@PostConstruct
	public void init() {

		SprintSearchConditions sprintSearchConditions = SprintSearchConditions
				.builder()
				.project("PROJECT_NAME")
				.sprint("SPRINT_NAME")
				.build();

		TasksTree tasksTree = jiraInformationHolder.getTasksAsTree(sprintSearchConditions);

		//		Gson gson = (new GsonBuilder()).create();
		//
		//		System.out.println(gson.toJson(tasksTree));

		tasksTree.getRootTasks().forEach(task -> {
			System.out.println(task.getId() + " " + task.getName());
			System.out.println(
					"Estimated " + convertMinutesToHour(task.getOriginalEstimateMinutes()) +
							" Real " + convertMinutesToHour(task.getTimeSpentMinutes()) +
							" Remaining " + convertMinutesToHour(task.getRemainingEstimateMinutes()));

			task.getWorkLogs().forEach(worklog -> {
				System.out.println(worklog.getPerson() + " " + worklog.getMinutesSpent() + " " + worklog.getStartDateTime());
			});

			task.getSubTasks().forEach(subTask -> {
				System.out.println("          " + subTask.getId() + " " + subTask.getName());
				System.out.println("          " +
						"Estimated " + subTask.getOriginalEstimateMinutes() +
						" Real " + subTask.getTimeSpentMinutes() +
						" Remaining " + subTask.getRemainingEstimateMinutes());

				subTask.getWorkLogs().forEach(worklog -> {
					System.out.println("          " + worklog.getPerson() + " "
							+ convertMinutesToHour(worklog.getMinutesSpent()) + " "
							+ worklog.getStartDateTime());
				});

				System.out.println("");
			});
			System.out.println("");

		});
	}

	private Integer  convertMinutesToHour(Integer minutes) {
		if (null == minutes || minutes.equals(0)) {
			return 0;
		}
		return minutes / 60;
	}

	public List<TasksTreeView> prepareTaskView(SprintSearchConditions sprintSearchConditions) {
		TasksTree tasksTree = jiraInformationHolder.getTasksAsTree(sprintSearchConditions);

		//		List<TasksTree> tasksTrees = projectsKeys.stream()
		//				.map(projectKey -> jiraInformationHolder.getTasksAsTree(projectKey, startDate, endDate))
		//				.collect(Collectors.toList());

		//		TasksTreeViewAggregator tasksTreeViewAggregator = new TasksTreeViewAggregator();

		//		return tasksTrees.stream()
		//				.map(tasksTreeViewAggregator::prepareTasksTreeView)
		//				.collect(Collectors.toList());
		return null;
	}

	public PeopleView preparePeopleView() {
		return new PeopleView();
	}

	public void prepareDayView() {
	}
}
