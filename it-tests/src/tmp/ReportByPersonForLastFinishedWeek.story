!-- Scenario:  Simple test to give JBehave a test drive
!--
!-- Given task:
!-- |Id     |Name    |Status   |Type |IsSubTask|OriginalEstimateMinutes|RemainingEstimateMinutes|TimeSpentMinutes|
!-- |TASK-38|Training|Reccurent|Story|false    |                       |0                       |5745            |
!--
!-- with work log:
!-- |Person|StartDateTime   |MinutesSpent|
!-- |user#1|2020-02-21T17:04|120         |
!-- |user#2|2020-02-27T16:13|60          |
!-- |user#3|2020-02-27T16:33|60          |
!-- |user#4|2020-03-02T16:33|120         |
!-- |user#2|2020-03-06T16:15|30          |
!-- |user#3|2020-03-10T19:09|90          |
!-- |user#2|2020-03-17T09:38|60          |
!-- |user#2|2020-03-18T09:38|30          |
!-- |user#2|2020-03-18T09:38|30          |
!-- |user#2|2020-03-18T09:39|405         |
!-- |user#2|2020-03-24T09:41|45          |
!-- |user#3|2020-04-06T19:56|75          |
!-- |user#3|2020-04-06T19:57|60          |
!-- |user#2|2020-04-06T17:48|60          |
!-- |user#5|2020-12-15T18:36|90          |
!-- |user#6|2020-12-15T19:02|90          |
!-- |user#7|2020-12-15T08:46|90          |
!-- |user#8|2020-12-15T09:16|90          |
!-- |user#6|2020-12-16T15:56|90          |
!-- |user#5|2020-12-16T16:23|90          |
!--
!-- Given task:
!-- |Id     |Name                           |Status   |Type |IsSubTask|OriginalEstimateMinutes|RemainingEstimateMinutes|TimeSpentMinutes|
!-- |TASK-32|Management, Reporting and Admin|Reccurent|Story|false    |                       |0                       |24568           |
!--
!-- with work log:
!-- |Person|StartDateTime   |MinutesSpent|
!-- |user#1|2020-02-18T16:30|240         |
!-- |user#1|2020-02-19T16:51|30          |
!-- |user#1|2020-02-21T17:01|145         |
!-- |user#1|2020-02-21T18:38|60          |
!-- |user#2|2020-02-24T18:03|35          |
!-- |user#1|2020-02-24T18:04|35          |
!-- |user#1|2020-02-24T19:05|180         |
!-- |user#4|2020-02-24T18:25|35          |
!-- |user#4|2020-02-28T16:19|30          |
!-- |user#4|2020-02-28T16:22|180         |
!-- |user#3|2020-02-28T16:41|240         |
!-- |user#4|2020-02-28T16:55|30          |
!-- |user#3|2020-02-28T17:09|30          |
!-- |user#4|2020-02-24T17:24|10          |
!-- |user#1|2020-03-02T17:20|60          |
!-- |user#1|2020-03-02T19:44|30          |
!-- |user#1|2020-03-05T08:59|120         |
!-- |user#1|2020-03-05T09:00|60          |
!-- |user#2|2020-03-05T16:05|30          |
!-- |user#4|2020-03-06T16:27|30          |
!--
!-- Given task:
!-- |Id       |Name                              |Status|Type |IsSubTask|OriginalEstimateMinutes|RemainingEstimateMinutes|TimeSpentMinutes|
!-- |TASK-2537|Sprint tasks, admin and management|To Do |Story|false    |3360                   |1490                    |1870            |
!--
!-- with work log:
!-- |Person|StartDateTime   |MinutesSpent|
!-- |user#6|2021-01-04T18:08|30          |
!-- |user#5|2021-01-04T18:11|60          |
!-- |user#5|2021-01-05T11:50|60          |
!-- |user#2|2021-01-05T11:53|30          |
!-- |user#2|2021-01-05T11:53|20          |
!-- |user#9|2021-01-04T11:57|60          |
!-- |user#9|2021-01-04T11:57|120         |
!-- |user#6|2021-01-05T13:58|30          |
!-- |user#1|2021-01-04T17:35|210         |
!-- |user#1|2021-01-05T17:40|15          |
!-- |user#1|2021-01-05T17:40|20          |
!-- |user#7|2021-01-04T18:47|60          |
!-- |user#7|2021-01-05T18:50|60          |
!-- |user#6|2021-01-06T12:26|30          |
!-- |user#2|2021-01-06T17:36|30          |
!-- |user#2|2021-01-06T17:36|15          |
!-- |user#5|2021-01-06T17:01|120         |
!-- |user#2|2021-01-07T09:23|30          |
!-- |user#2|2021-01-08T11:16|40          |
!-- |user#2|2021-01-08T11:59|15          |
!--
!-- Given task:
!-- |Id       |Name                                                                    |Status|Type|IsSubTask|OriginalEstimateMinutes|RemainingEstimateMinutes|TimeSpentMinutes|
!-- |TASK-2522|Upgrade lib version to 1.2.21 of platform config in trading calendar soa|Done  |Task|false    |300                    |0                       |150             |
!--
!-- with work log:
!-- |Person|StartDateTime   |MinutesSpent|
!-- |user#2|2020-12-31T14:58|60          |
!-- |user#5|2020-12-31T15:32|90          |
!--
!-- Given task:
!-- |Id       |Name                             |Status|Type    |IsSubTask|OriginalEstimateMinutes|RemainingEstimateMinutes|TimeSpentMinutes|
!-- |TASK-2521|Test returned value from soa call|Done  |Sub-task|true     |780                    |0                       |340             |
!--
!-- with work log:
!-- |Person|StartDateTime   |MinutesSpent|
!-- |user#5|2020-12-31T15:32|300         |
!-- |user#5|2020-12-31T17:23|30          |
!-- |user#2|2021-01-04T12:11|10          |
!--
!-- Given task:
!-- |Id       |Name                             |Status|Type    |IsSubTask|OriginalEstimateMinutes|RemainingEstimateMinutes|TimeSpentMinutes|
!-- |TASK-2518|Crete steps for testing web rates|Done  |Sub-task|true     |480                    |0                       |150             |
!--
!-- with work log:
!-- |Person|StartDateTime   |MinutesSpent|
!-- |user#5|2020-12-29T19:10|90          |
!-- |user#5|2020-12-29T19:12|60          |
!--
!-- Given task:
!-- |Id       |Name                                        |Status|Type    |IsSubTask|OriginalEstimateMinutes|RemainingEstimateMinutes|TimeSpentMinutes|
!-- |TASK-2509|Calculate the swap rates for margin type Add|Done  |Sub-task|true     |780                    |0                       |740             |
!--
!-- with work log:
!-- |Person|StartDateTime   |MinutesSpent|
!-- |user#5|2020-12-24T17:37|90          |
!-- |user#5|2020-12-24T14:38|240         |
!-- |user#5|2020-12-28T16:19|90          |
!-- |user#5|2020-12-28T16:54|120         |
!-- |user#2|2020-12-30T14:46|20          |
!-- |user#6|2021-01-05T10:01|60          |
!-- |user#7|2021-01-05T18:50|120         |
!--
!-- Given task:
!-- |Id       |Name                              |Status|Type |IsSubTask|OriginalEstimateMinutes|RemainingEstimateMinutes|TimeSpentMinutes|
!-- |TASK-2503|Sprint tasks, admin and management|Done  |Story|false    |3360                   |60                      |5280            |
!--
!-- with work log:
!-- |Person |StartDateTime   |MinutesSpent|
!-- |user#10|2020-12-17T09:32|60          |
!-- |user#10|2020-12-17T09:34|120         |
!-- |user#10|2020-12-17T10:35|30          |
!-- |user#5 |2020-12-17T12:52|30          |
!-- |user#6 |2020-12-17T13:08|30          |
!-- |user#8 |2020-12-17T13:25|30          |
!-- |user#7 |2020-12-17T13:53|90          |
!-- |user#5 |2020-12-17T17:32|30          |
!-- |user#10|2020-12-18T08:56|60          |
!-- |user#2 |2020-12-18T11:35|30          |
!-- |user#2 |2020-12-18T11:35|15          |
!-- |user#3 |2020-12-17T12:05|45          |
!-- |user#3 |2020-12-18T12:05|20          |
!-- |user#3 |2020-12-17T12:05|20          |
!-- |user#5 |2020-12-18T12:35|30          |
!-- |user#3 |2020-12-18T12:05|20          |
!-- |user#3 |2020-12-17T12:05|20          |
!-- |user#2 |2020-12-17T15:47|60          |
!-- |user#2 |2020-12-17T15:48|20          |
!-- |user#2 |2020-12-18T16:03|30          |
!--
!-- Given task:
!-- |Id       |Name       |Status|Type    |IsSubTask|OriginalEstimateMinutes|RemainingEstimateMinutes|TimeSpentMinutes|
!-- |TASK-2500|Code review|Done  |Sub-task|true     |                       |0                       |450             |
!--
!-- with work log:
!-- |Person |StartDateTime   |MinutesSpent|
!-- |user#10|2020-12-21T11:24|30          |
!-- |user#7 |2020-12-23T08:49|30          |
!-- |user#6 |2020-12-28T20:43|90          |
!-- |user#5 |2020-12-29T11:17|30          |
!-- |user#7 |2020-12-29T20:44|60          |
!-- |user#5 |2020-12-30T17:14|150         |
!-- |user#5 |2021-01-05T16:12|60          |
!--
!-- Given task:
!-- |Id       |Name       |Status|Type    |IsSubTask|OriginalEstimateMinutes|RemainingEstimateMinutes|TimeSpentMinutes|
!-- |TASK-2493|Code review|To Do |Sub-task|true     |                       |0                       |30              |
!--
!-- with work log:
!-- |Person|StartDateTime   |MinutesSpent|
!-- |user#5|2020-12-29T11:17|30          |
!--
!-- Given task:
!-- |Id       |Name                                          |Status|Type    |IsSubTask|OriginalEstimateMinutes|RemainingEstimateMinutes|TimeSpentMinutes|
!-- |TASK-2471|Calculate the swap rates for rule type Default|Done  |Sub-task|true     |780                    |0                       |1180            |
!--
!-- with work log:
!-- |Person|StartDateTime   |MinutesSpent|
!-- |user#8|2020-12-24T17:14|90          |
!-- |user#8|2020-12-24T17:14|90          |
!-- |user#8|2020-12-28T19:09|180         |
!-- |user#8|2020-12-28T19:09|90          |
!-- |user#2|2020-12-30T15:04|10          |
!-- |user#5|2021-01-04T18:13|360         |
!-- |user#5|2021-01-05T13:48|240         |
!-- |user#7|2021-01-04T18:49|120         |
!--
!-- Given task:
!-- |Id       |Name                                                          |Status|Type |IsSubTask|OriginalEstimateMinutes|RemainingEstimateMinutes|TimeSpentMinutes|
!-- |TASK-2463|Blue Chips - Swaps - Merge of branches with technical upgrades|Done  |Story|false    |780                    |0                       |270             |
!--
!-- with work log:
!-- |Person|StartDateTime   |MinutesSpent|
!-- |user#5|2020-12-30T17:15|270         |
!--
!-- Given task:
!-- |Id       |Name                                                   |Status        |Type    |IsSubTask|OriginalEstimateMinutes|RemainingEstimateMinutes|TimeSpentMinutes|
!-- |TASK-2402|Support extraction instrument parameters for CFD Equity|To test on Pre|Sub-task|true     |780                    |0                       |1970            |
!--
!-- with work log:
!-- |Person|StartDateTime   |MinutesSpent|
!-- |user#7|2020-12-10T19:47|120         |
!-- |user#7|2020-12-11T19:48|420         |
!-- |user#7|2020-12-14T21:43|420         |
!-- |user#7|2020-12-16T22:21|90          |
!-- |user#7|2020-12-15T22:22|240         |
!-- |user#7|2020-12-15T09:52|90          |
!-- |user#7|2020-12-16T09:54|90          |
!-- |user#7|2020-12-18T09:15|90          |
!-- |user#7|2020-12-21T18:12|30          |
!-- |user#7|2020-12-23T08:41|60          |
!-- |user#7|2020-12-24T18:10|30          |
!-- |user#5|2020-12-29T19:11|120         |
!-- |user#7|2020-12-29T20:45|90          |
!-- |user#2|2020-12-30T11:41|20          |
!-- |user#7|2020-12-30T08:45|60          |
!--
!-- Given task:
!-- |Id       |Name                                     |Status|Type    |IsSubTask|OriginalEstimateMinutes|RemainingEstimateMinutes|TimeSpentMinutes|
!-- |TASK-2333|Prepare version update for: sq-parent-pom|Done  |Sub-task|true     |                       |0                       |90              |
!--
!-- with work log:
!-- |Person|StartDateTime   |MinutesSpent|
!-- |user#7|2020-11-18T10:11|30          |
!-- |user#5|2021-01-04T18:15|60          |
!--
!-- Given request with response
!--
!-- When prepare report for person user#5 for last finished week executed at date 2021-01-11
!--
!-- Then PeopleView common fields is:
!-- |TeamName|StartPeriod|EndPeriod |
!-- |Ducks   |2021-01-04 |2021-01-11|
!--
!-- Then PeopleView PersonMetrics present for persons: user#5
!--
!-- Then PeopleView PersonMetrics common fields is:
!-- |Person|TotalTimeSpentInCurrentPeriodInMinutes|TotalTimeSpentOnTaskInMinutes|
!-- |user#5|960                                   |2940                         |
!--
!-- Then PeopleView PersonMetrics DailyTaskLogs for user#5 is:
!-- |TaskId   |TotalTimeSpentByPeriodInMinutes|TotalTimeSpentOnTaskInMinutes|TimeSpentMinutes|OriginalEstimateMinutes|TimeCoefficient|TaskName                                                                |TaskExternalStatus|
!-- |TASK-38  |0                              |180                          |180             |0                      |0.0            |Training                                                                |Reccurent         |
!-- |TASK-2537|240                            |240                          |240             |3360                   |1.7967914      |Sprint tasks, admin and management                                      |To Do             |
!-- |TASK-2522|0                              |90                           |90              |300                    |2.0            |Upgrade lib version to 1.2.21 of platform config in trading calendar soa|Done              |
!-- |TASK-2521|0                              |330                          |330             |780                    |2.2941177      |Test returned value from soa call                                       |Done              |
!-- |TASK-2518|0                              |150                          |150             |480                    |3.2            |Crete steps for testing web rates                                       |Done              |
!-- |TASK-2509|0                              |540                          |540             |780                    |1.054054       |Calculate the swap rates for margin type Add                            |Done              |
!-- |TASK-2503|0                              |90                           |90              |3360                   |0.6363636      |Sprint tasks, admin and management                                      |Done              |
!-- |TASK-2500|60                             |240                          |240             |0                      |0.0            |Code review                                                             |Done              |
!-- |TASK-2493|0                              |30                           |30              |0                      |0.0            |Code review                                                             |To Do             |
!-- |TASK-2471|600                            |600                          |600             |780                    |0.66101694     |Calculate the swap rates for rule type Default                          |Done              |
!-- |TASK-2463|0                              |270                          |270             |780                    |2.8888888      |Blue Chips - Swaps - Merge of branches with technical upgrades          |Done              |
!-- |TASK-2402|0                              |120                          |120             |780                    |0.39593908     |Support extraction instrument parameters for CFD Equity                 |To test on Pre    |
!-- |TASK-2333|60                             |60                           |60              |0                      |0.0            |Prepare version update for: sq-parent-pom                               |Done              |
!--
!-- Then PeopleView PersonMetrics DailyTaskLogs TimeSpentByDay in Minutes for user#5 is:
!-- |TaskId|2020-12-15|2020-12-16|2020-12-17|2020-12-18|2020-12-24|2020-12-28|2020-12-29|2020-12-30|2020-12-31|2021-01-04|2021-01-05|2021-01-06|
!-- |TASK-38|90|90|
!-- |TASK-2537||||||||||60|60|120|
!-- |TASK-2522|||||||||90 |
!-- |TASK-2521|||||||||330|
!-- |TASK-2518|||||||150|
!-- |TASK-2509|||||330|210|
!-- |TASK-2503|||60|30|
!-- |TASK-2500|||||||30|150|||60|
!-- |TASK-2493|||||||30|
!-- |TASK-2471||||||||||360|240|
!-- |TASK-2463||||||||270|
!-- |TASK-2402|||||||120|
!-- |TASK-2333||||||||||60|
!--
!-- Then PeopleView PersonMetrics DailyTaskLogs Total TimeSpentByDay in Minutes for user#5 is:
!-- |2020-12-15|2020-12-16|2020-12-17|2020-12-18|2020-12-24|2020-12-28|2020-12-29|2020-12-30|2020-12-31|2021-01-04|2021-01-05|2021-01-06|
!-- |90        |90        |60        |30        |330       |210       |330       |420       |420       |480       |360       |120       |