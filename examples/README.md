# Examples
Different Spring Boot App to play with Camundala.

To let them work together and run them in parallel, 
we use a Postgres Database that is shared between the examples.

Make sure each example runs on its own port.

## List of examples

Example | Port | Http Port | README
:--- | ---: | ---:  | :---
Camunda Rest API | `9997` | - | [./rest/README.m](./rest/README.md)
Twitter Demo App | `9998` | `8888`| [./twitter/README.m](./twitter/README.md)
Playground App | `10001` | `9001`| [./playground/README.m](./playground/README.md)

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