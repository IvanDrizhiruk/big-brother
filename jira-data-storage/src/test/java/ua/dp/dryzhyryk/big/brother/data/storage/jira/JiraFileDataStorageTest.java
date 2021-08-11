package ua.dp.dryzhyryk.big.brother.data.storage.jira;

class JiraFileDataStorageTest {

//	private static final String ROOT_TEST_STORAGE_PATH = "target/test_storage/";
//
//	@BeforeAll
//	static void init() {
//		new File(ROOT_TEST_STORAGE_PATH).mkdirs();
//	}
//
//	@Test
//	void tasksShouldBeSavedAndLoadedwithoutChanges() {
//		//given
//		SprintSearchConditions sprintSearchConditions = SprintSearchConditions.builder()
//				.project("Storarge prj1")
//				.sprint("sprint-1")
//				.build();
//
//		Task subTask1_1 = Task.builder()
//				.id("#2")
//				.name("Empty tsk")
//				.build();
//
//		TaskWorkLog worklog1_1 = TaskWorkLog.builder()
//				.person("main")
//				.startDateTime(LocalDateTime.of(2019, 11, 21, 12, 45))
//				.minutesSpent(30)
//				.build();
//		Task task1 = Task.builder()
//				.id("#1")
//				.name("Task with different symbols !@#$%^&*()_+")
//				.originalEstimateMinutes(-123)
//				.remainingEstimateMinutes(123)
//				.timeSpentMinutes(1234567890)
//				.subTasks(Collections.singletonList(subTask1_1))
//				.workLogs(Collections.singletonList(worklog1_1))
//				.build();
//		Task task2 = Task.builder()
//				.id("#2")
//				.name("Empty tsk")
//				.build();
//		Task task3 = Task.builder()
//				.id("#3")
//				.name("Partially filled task")
//				.originalEstimateMinutes(10)
//				.remainingEstimateMinutes(0)
//				.timeSpentMinutes(25)
//				//				.subTasks()
//				//				.workLogs()
//				.build();
//
//		List<Task> tasksForSave = Arrays.asList(task1, task2, task3);
//
//		//when
//		JiraFileDataStorage jiraFileDataStorage = new JiraFileDataStorage(ROOT_TEST_STORAGE_PATH);
//		jiraFileDataStorage.saveProjectSprint(sprintSearchConditions, tasksForSave);
//		List<Task> loadedTasks = jiraFileDataStorage.loadTasks(sprintSearchConditions);
//
//		//than
//		Assertions.assertEquals(tasksForSave, loadedTasks);
//	}
}