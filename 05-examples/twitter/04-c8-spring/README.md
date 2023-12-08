# NOT IN USE - must be refactored to new structure
- see build.sbt

--- 

# Camunda Cloud / Spring Boot process solution

The self-contained process solution contains

* The process model as BPMN (auto-deployed during startup)
* Glue code for the service task
* REST endpoint that then starts a process instance
* Test case

Requirements:

* Java >= 8
* Maven

## How to run

* Download/clone the code in this folder.
* You need to set your Camunda cloud client connection details in the file `application.properties`. Simply replace the existing sample values.
* Run the application:

```
mvn package exec:java
```

```
curl -i -X PUT http://localhost:8323/tweet \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -d '{
  "tweet":"What the f***!",
  "author":"Sergey",
  "boss":"Putin"
}'
```

* You should see something like this:
