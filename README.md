# big-brother

## How to run

* In progress

https://docs.atlassian.com/jira-software/REST/7.3.1/?_ga=2.94652252.1586695115.1574692296-579008930.1572851858#agile/1.0/sprint-getSprint


https://jira.bank.swissquote.ch/rest/agile/1.0/board/?startAt=150
https://jira.bank.swissquote.ch/rest/agile/1.0/board/1296/sprint
https://jira.bank.swissquote.ch/rest/agile/1.0/board/1296/sprint/
https://jira.bank.swissquote.ch/rest/agile/1.0/sprint/5023
https://jira.bank.swissquote.ch/rest/agile/1.0/sprint/4960/issue
https://jira.bank.swissquote.ch/rest/agile/1.0/sprint/4960/issue


worklogAuthor = o_dkoval 

assignee = o_dkoval 

worklogAuthor = o_dkoval AND  worklogDate >=  2019-11-11 AND  worklogDate <= 2019-11-15


Sonar
mvn clean verify -Dsonar.host.url=https://sonarcloud.io org.sonarsource.scanner.maven:sonar-maven-plugin:3.7.0.1746:sonar -Dsonar.organization=ivandrizhiruk -Dsonar.projectKey=IvanDrizhiruk_big-brother
