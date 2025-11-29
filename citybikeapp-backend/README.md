# City Bike App backend

Note that application can be used to automatically fetch and load data that is owned by City Bike Finland (journey data)
and Helsingin seudun liikenne (HSL) (station data). Data is licensed
under [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/) as of 2nd January 2023.

## Getting started

Application uses a Gradle Wrapper to provide a consistent Gradle version. All actions / tasks are invoked through the
wrapper script file which is `gradlew.bat` for Windows, `gradlew` otherwise.

### Requirements

#### Development / running locally

* Java 21
    * For example, use [Eclipse Temurin](https://adoptium.net/temurin/releases/)
* PostgreSQL 15 database access for jOOQ code generation that in turn depends on Flyway migrations for the schema
    * App assumes an existing PostgreSQL 15 instance to be available at `postgresql://host.docker.internal:5432/citybikeapp`
      with credentials `postgres:Hunter2`.

      Run one for example with docker
      using `docker run -d --restart --name dev-postgres -p 5432:5432 -e POSTGRES_USER=postgres -e POSTGRES_DB=citybikeapp -e POSTGRES_PASSWORD=Hunter2 postgres:15`
* Tests are run against a Testcontainers provided PostgreSQL 15
    * Docker host is required for Testcontainers. Podman should work.
* Run dataloading using Gradle task `bootRun` with `dataloader` Spring profile, for example
  with `SPRING_PROFILES_ACTIVE=dataloader ./gradlew bootRun`
    * If you need to use a different DB configuration, see [Environment variables](#environment-variables).
    * You could, for example, explicitly use `localhost` (default `host.docker.internal` should be equivalent) by using
      `DATABASE_CONNECTION_URL=jdbc:postgresql://localhost:5432/citybikeapp ./gradlew run`.
        * Windows terminals need some additional tending to set env vars: `SET FOO=bar` or `$env:FOO='bar'`
* Run application using Gradle task `bootRun`, for example with `./gradlew bootRun`
* Application API will be available under http://localhost:8080/
    * See [api.yml](gen/api.yml) or [https://mtuomiko.github.io/citybikeapp/](https://mtuomiko.github.io/citybikeapp/)
      for available endpoints

#### Running in container

* Java 21
* PostgreSQL 15 database access
    * jOOQ minimum supported version. Might work on older, I haven't tested.
* ~300 extra MB of space on filesystem for downloading datasets

### Gradle tasks

Few relevant Gradle tasks. Run using the Gradle wrapper. For example, `./gradlew build`

* `build` check everything and compile
* `clean` clear anything that Gradle has created in the project. Use for example with build if something seems to be
  fubar: `clean build`
* `generateJooq` run jOOQ code generation explicitly. Created code is located at `build/generated-src/jooq`
* `check` run verification tasks
    * `detekt` run detekt Kotlin static code analysis
    * `spotlessCheck` run spotless code style checks
        * `spotlessApply` to autofix
    * `test` run all tests
* `jacocoTestReport` create jacoco test coverage report to `build/reports/jacoco`
* `dockerfile` to generate a layered Dockerfile to `build/docker/main`

## Data loader

Application can be started in a single run data loading mode by using the Spring profile `dataloader`. In this mode, the
actual server will not be started and the application will exit after completion.

Data loader will read the provided configuration and download CSV files to batch insert their data to the database.
Loader will only delete the used local files when running in `prod` environment. This means that downloaded data can
remain in place, in containers also.

Loader will perform simple validation and cleaning on the data. Anything not matching the assumed format or data model,
will cause the entry to be ignored. Duplicate entries are ignored. For stations the primary key ID is read straight
from source CSV and subsequent INSERTs on same ID are ignored. For journeys the uniqueness in maintained by an all
column unique constraint/index, a bit doubtful about this... (temp table on insert could work also?)

Example for running data loader: `SPRING_PROFILES_ACTIVE=dataloader ./gradlew bootRun`

## Code generation and API generation

Backend DAO layer implementation depends on jOOQ codegeneration (data about tables and columns and so on). It needs
some schema to use for generation and here the schema is being formed in a temporary PostgreSQL testcontainer by running
Flyway migrations on it. Meaning the build doesn't depend on an already existing DB, but a docker host is required.

API specifications are being generated in [gen/api.yml](gen/api.yml) based on the controllers and API response classes.
The spec isn't too pretty, API-first approach would probably be nicer and result in more coherent spec file.

## Environment variables

This table describes relevant variables when running the application in production mode. Spring allows property
configuration by binding automatically from environment variables, but these are also explicitly configured.

#### Common

| Environment variable            | Description                                                  | Default                                                   | Required | Example                                      |
|---------------------------------|--------------------------------------------------------------|-----------------------------------------------------------|----------|----------------------------------------------|
| `PORT`                          | Server port                                                  | `8080`                                                    |          |                                              |
| `DATABASE_CONNECTION_URL`       | JDBC connection URL                                          | `jdbc:postgresql://host.docker.internal:5432/citybikeapp` |          | `jdbc:postgresql://foo.bar:5432/citybikeapp` |
| `DATABASE_CONNECTION_USERNAME`  | DB username                                                  | `postgres`                                                |          | `foo`                                        |
| `DATABASE_CONNECTION_PASSWORD`  | DB password                                                  | `Hunter2`                                                 |          | `bar`                                        |
| `CITYBIKEAPP_DEFAULT_PAGE_SIZE` | Default page size used in paginating queries                 | `50`                                                      |          |                                              |
| `CITYBIKEAPP_MAX_PAGE_SIZE`     | Maximum client provided page size used in paginating queries | `50`                                                      |          |                                              |

#### Data loader specific environment variables

| Environment variable                            | Description                                             | Default                 | Example                                                   |
|-------------------------------------------------|---------------------------------------------------------|-------------------------|-----------------------------------------------------------|
| `CITYBIKEAPP_DATALOADER_MINIMUMJOURNEYDISTANCE` | Minimum filter on journey length (meters)               | `10`                    |                                                           |
| `CITYBIKEAPP_DATALOADER_MINIMUMJOURNEYDURATION` | Minimum filter on journey duration (seconds)            | `10`                    |                                                           |
| `CITYBIKEAPP_DATALOADER_BATCHSIZE`              | Database batch size for inserts                         | `1000`                  |                                                           |
| `CITYBIKEAPP_DATALOADER_STATIONURL`             | URL for station data CSV file                           | [[1]](#default_station) | `http://foo.bar/file.csv`                                 |
| `CITYBIKEAPP_DATALOADER_JOURNEYURLS`            | Comma separated list of URLs for journey data CSV files | [[2]](#default_journey) | `http://foo.bar/journey1.csv,http://foo.bar/journey2.csv` |

<a id="default_station"></a>[1] `https://opendata.arcgis.com/datasets/726277c507ef4914b0aec3cbcfcbfafc_0.csv`

<a id="default_journey"></a>[2] `https://dev.hsl.fi/citybikes/od-trips-2021/2021-05.csv,https://dev.hsl.fi/citybikes/od-trips-2021/2021-06.csv,https://dev.hsl.fi/citybikes/od-trips-2021/2021-07.csv`

## Used technologies

* Framework: Spring Boot. Fairly known and used (plus managed to use code generation from OpenAPI specs fairly easily).
    * Started off using Micronaut since I wanted to see how these new AOT focused frameworks do. Original motivation came from when I was running a different project's Spring Boot backend on a free deployment at Render.com, and it took 5 minutes to start the container. Obviously the free tier had very limited resources but still. However, Micronaut seemed to not get traction, and I wanted to explore other options.
    * Tried to transition to Quarkus, but I also wanted API Interface / Controller code generation from OpenAPI specs. This ended up being a deepish rabbit hole with no solution in sight that would not require some string replacement "hacks" in order to create compilable classes. So basically I could not find a suitable generator that would work with a modern Quarkus version. (jaxrs-spec OpenAPI server generator)[https://openapi-generator.tech/docs/generators/jaxrs-spec] came close but that's still on `javax` namespace with no working option to use `jakarta`, so still not compatible.
* "API first" for code-generation (not really a technology).
    * I'd argue that it's usually beneficial to tie the API specification to the actual code programmatically. It's much harder for the implementation to differ from the API spec when you have this connection, one way or other. Generating the API from code seemed to be straight-forward in simple cases, but actually writing the API descriptions etc. using annotations was cumbersome. So that's the motivation for generating code from the API.
    * It's a trade-off. I'm currently veering back towards code-first approach. It places no limitations on 
* Build/tooling: Gradle
    * Because Kotlin. Not a necessity but it's a more established choice in Kotlin-based projects.
* Language: Kotlin
    * I just like it :)
* Database: PostgreSQL. I've worked mostly with this RDBMS, and haven't really found a reason to explore other options.
* DB mapping/access: jOOQ
    * JPA/Hibernate didn't seem particularly useful here as we're not using any complex relationships between entities. The more complex statistics queries would need to be written in SQL anyway.
    * Jdbi 3 was an option, but seemed like too much manual SQL. 
    * jOOQ gives the option to write the queries "in code" rather than just SQL strings. I like the option, at least for this single-person project.
* Migrations: Flyway
    * Fairly commonly used. No particular reason other than that. Liquibase would probably be the most common alternative, but haven't used it.
* Code quality / static analysis: detekt, Spotless
* Test coverage: JaCoCo

## TODOs

* ???
