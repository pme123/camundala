# Examples
Different Spring Boot App to play with Camundala.

Make sure only one example runs at the time as they have the same ports configured.

## List of examples

Example | README
:--- | :---
Camunda Rest API | [./rest/README.m](./rest/README.md)
Twitter Demo App | [./twitter/README.m](./twitter/README.md)
Playground App | [./playground/README.m](./playground/README.md)

# Docker
We provide a `docker-compose.yml` file for the Database.
## Usage
Go to Docker directory:
`cd ./examples/docker`
### start
`docker-compose -p camunda-db up -d`
### down
`docker-compose -p camunda-db down`
### down (removing Database)
`docker-compose -p camunda-db down -v`

# Running an example In Memory
If you do not want to setup Docker, just comment out the Postgres
configuration in the `application.yml`, like:
```yaml
#    database:
#      schema-update: true
#      type: postgres

#spring:
#  datasource:
#    platform: postgres
#    url: "jdbc:postgresql://localhost:5432/camunda"
#    username: postgres
#    password: Initial@Password
#    driverClassName: org.postgresql.Driver
#    initialization-mode: always
#
#    main.banner-mode: off
```

# Start your own Project
Check out the [Starter App](https://github.com/pme123/camundala-starter)
