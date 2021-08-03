Lifecycle:
Before:
Given cleanup jira response

Scenario:  Weekly table with total fields should be calculated for person

Given task:
|Id     |Name    |Status   |Type |IsSubTask|OriginalEstimateMinutes|RemainingEstimateMinutes|TimeSpentMinutes|
|TASK-1 |Training|Reccurent|Story|false    |                       |100                     |150             |

with work log:
|Person|StartDateTime   |MinutesSpent|
|user#1|2021-03-01T17:04|30          |
|user#1|2021-03-02T17:04|35          |
|user#1|2021-03-03T17:04|15          |
|user#1|2021-03-05T17:04|5           |
|user#1|2021-03-06T17:04|20          |
|user#1|2021-03-07T17:04|15          |
|user#1|2021-03-07T17:30|10          |

Given task:
|Id     |Name                           |Status   |Type |IsSubTask|OriginalEstimateMinutes|RemainingEstimateMinutes|TimeSpentMinutes|
|TASK-2 |Management, Reporting and Admin|Reccurent|Story|false    |                       |0                       |180             |

with work log:
|Person|StartDateTime   |MinutesSpent|
|user#1|2021-03-01T15:04|30          |
|user#1|2021-03-01T16:04|60          |
|user#1|2021-03-01T17:04|30          |


Given task:
|Id       |Name                              |Status|Type |IsSubTask|OriginalEstimateMinutes|RemainingEstimateMinutes|TimeSpentMinutes|
|TASK-3   |Sprint tasks, admin and management|To Do |Story|false    |120                    |0                       |90              |

with work log:
|Person|StartDateTime   |MinutesSpent|
|user#1|2021-03-02T17:04|90          |

Given task:
|Id       |Name         |Status|Type|IsSubTask|OriginalEstimateMinutes|RemainingEstimateMinutes|TimeSpentMinutes|
|TASK-4   |Upgrade libs |Done  |Task|false    |60                     |0                       |30              |

with work log:
|Person|StartDateTime   |MinutesSpent|
|user#1|2021-03-04T14:58|30          |


Given task:
|Id       |Name                          |Status|Type    |IsSubTask|OriginalEstimateMinutes|RemainingEstimateMinutes|TimeSpentMinutes|
|TASK-5|Test returned value from soa call|Done  |Sub-task|true     |17                     |0                       |30              |

with work log:
|Person|StartDateTime   |MinutesSpent|
|user#1|2021-03-05T14:58|30          |

Given request with response

When prepare report for person user#1 for last finished week executed at date 2021-03-11

Then PeopleView PersonMetrics present for persons: user#1

Then PeopleView PersonMetrics DailyTaskLogs TimeSpentByDay in Minutes for user#1 is:
|TaskId   |2021-03-01|2021-03-02|2021-03-03|2021-03-04|2021-03-05|2021-03-06|2021-03-07|
|TASK-1   |30        |35        |15        |          |5         |20        |25        |
|TASK-2   |120       |          |          |          |          |          |          |
|TASK-3   |          |90        |          |          |          |          |          |
|TASK-4   |          |          |          |30        |          |          |          |
|TASK-5   |          |          |          |          |30        |          |          |

Then PeopleView PersonMetrics DailyTaskLogs Total TimeSpentByDay in Minutes for user#1 is:
|2021-03-01|2021-03-02|2021-03-03|2021-03-04|2021-03-05|2021-03-06|2021-03-07|
|150       |125       |15        |30        |35        |20        |25        |


Then PeopleView PersonMetrics DailyTaskLogs for user#1 is:
|TaskId   |TotalTimeSpentByPeriodInMinutes|TotalTimeSpentOnTaskInMinutes|TimeSpentMinutes|OriginalEstimateMinutes|TimeCoefficient|TaskName                          |TaskExternalStatus|
|TASK-1   |130                            |130                          |130             |0                      |0.0            |Training                          |Reccurent         |
|TASK-2   |120                            |120                          |120             |0                      |0.0            |Management, Reporting and Admin   |Reccurent         |
|TASK-3   |90                             |90                           |90              |120                    |1.3333334      |Sprint tasks, admin and management|To Do             |
|TASK-4   |30                             |30                           |30              |60                     |2.0            |Upgrade libs                      |Done              |
|TASK-5   |30                             |30                           |30              |17                     |0.56666666     |Test returned value from soa call |Done              |


Scenario:  External days and person should be ignored on weekly table calculatin

Given task:
|Id     |Name    |Status   |Type |IsSubTask|OriginalEstimateMinutes|RemainingEstimateMinutes|TimeSpentMinutes|
|TASK-1 |Training|Reccurent|Story|false    |                       |100                     |150             |

with work log:
|Person|StartDateTime   |MinutesSpent|
|user#1|2021-02-15T17:04|30          |
|user#2|2021-03-02T17:04|35          |
|user#3|2021-03-03T17:04|15          |
|user#1|2021-03-05T17:04|5           |
|user#1|2021-03-10T17:04|20          |

Given task:
|Id     |Name                           |Status   |Type |IsSubTask|OriginalEstimateMinutes|RemainingEstimateMinutes|TimeSpentMinutes|
|TASK-2 |Management, Reporting and Admin|Reccurent|Story|false    |                       |0                       |180             |

with work log:
|Person|StartDateTime   |MinutesSpent|
|user#1|2021-02-25T15:04|30          |
|user#2|2021-03-01T16:04|60          |
|user#1|2021-03-01T17:04|30          |


Given task:
|Id       |Name                              |Status|Type |IsSubTask|OriginalEstimateMinutes|RemainingEstimateMinutes|TimeSpentMinutes|
|TASK-3   |Sprint tasks, admin and management|To Do |Story|false    |120                    |0                       |90              |

with work log:
|Person|StartDateTime   |MinutesSpent|
|user#1|2021-03-02T17:04|90          |

Given request with response

When prepare report for person user#1 for last finished week executed at date 2021-03-11

Then PeopleView PersonMetrics present for persons: user#1

Then PeopleView PersonMetrics DailyTaskLogs TimeSpentByDay in Minutes for user#1 is:
|TaskId   |2021-02-15|2021-02-25|2021-03-01|2021-03-02|2021-03-05|2021-03-10|
|TASK-1   |30        |          |          |          |5         |20        |
|TASK-2   |          |30        |30        |          |          |          |
|TASK-3   |          |          |          |90        |          |          |

Then PeopleView PersonMetrics DailyTaskLogs Total TimeSpentByDay in Minutes for user#1 is:
|2021-02-15|2021-02-25|2021-03-01|2021-03-02|2021-03-05|2021-03-10|
|30        |30        |30        |90        |5         |20        |

Then PeopleView PersonMetrics DailyTaskLogs for user#1 is:
|TaskId   |TotalTimeSpentByPeriodInMinutes|TotalTimeSpentOnTaskInMinutes|TimeSpentMinutes|OriginalEstimateMinutes|TimeCoefficient|TaskName                          |TaskExternalStatus|
|TASK-1   |5                              |55                           |55              |0                      |0.0            |Training                          |Reccurent         |
|TASK-2   |30                             |60                           |60              |0                      |0.0            |Management, Reporting and Admin   |Reccurent         |
|TASK-3   |90                             |90                           |90              |120                    |1.3333334      |Sprint tasks, admin and management|To Do             |

