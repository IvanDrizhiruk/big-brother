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


## Sonar

For build project with sonar analysing you should use maven build with sonar profile 
```
mvn install -Psonar
```

### Docker wit sonar
```
docker run -d --name sonarqube -p 9000:9000 sonarqube:7.5-community
```
Sonar will be available by address: http://localhost:9000/

### Other possible options for build

mvn clean verify sonar:sonar \
-Dsonar.projectKey=IvanDrizhiruk_big-brother \
-Dsonar.host.url=http://localhost:9000 \
-Dsonar.login=435375c0c39eaf23eff3073214ba4f28fc89c615

mvn clean verify
-Dsonar.host.url=https://sonarcloud.io org.sonarsource.scanner.maven:sonar-maven-plugin:3.7.0.1746:sonar 
-Dsonar.organization=ivandrizhiruk 
-Dsonar.projectKey=IvanDrizhiruk_big-brother 
-Dsonar.token=big-brother-token

mvn clean verify 
org.jacoco:jacoco-maven-plugin:prepare-agent 
-Dsonar.host.url=https://sonarcloud.io 
org.sonarsource.scanner.maven:sonar-maven-plugin:3.7.0.1746:sonar 
-Dsonar.organization=ivandrizhiruk 
-Dsonar.projectKey=IvanDrizhiruk_big-brother 
-Dsonar.token=big-brother-token


#TODO
- Support assigned persons for tasks
- Support statuses 