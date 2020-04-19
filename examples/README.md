# Examples
Different Spring Boot App to play with Camundala.

To let them work together, we use a Postgres Database.

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