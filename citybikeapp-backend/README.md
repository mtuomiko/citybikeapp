# City Bike App backend

Note that application can be used to automatically fetch and load data that is owned by City Bike Finland (journey data)
and Helsingin seudun liikenne (HSL) (station data). Data is licensed
under [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/) as of 2nd January 2023.

## Getting started

Application uses a Gradle Wrapper to provide a consistent Gradle version. All actions / tasks are invoked through the
wrapper script file which is `gradlew.bat` for Windows, `gradlew` otherwise.

### Requirements

* Java JDK 17
    * For example, use [Eclipse Temurin](https://adoptium.net/temurin/releases/)
* Docker host (for tests and jOOQ code generation that runs against a temporary PostgreSQL testcontainer). Podman might
  work also.
    * See [http://www.docker.com](https://www.docker.com/). Docker Desktop is not free for businesses.
    * (or [https://podman.io/](https://podman.io/))
* PostgreSQL 14 database access (when running)

### Running locally

* App assumes an existing PostgreSQL 14 instance to be available at `postgresql://host.docker.internal:5432/citybikeapp`
  with credentials `postgres:Hunter2`.

  Run one for example with docker
  using `docker run -d --restart --name dev-postgres -p 5432:5432 -e POSTGRES_USER=postgres -e POSTGRES_DB=citybikeapp -e POSTGRES_PASSWORD=Hunter2 postgres:14`
* Run dataloading using Gradle task `run` with `dataloader` argument, for example
  with `./gradlew run --args "dataloader"`
    * If you need to use a different DB configuration, see [Environment variables](#environment-variables).
    * You could, for example, explicitly use `localhost` (default `host.docker.internal` should be equivalent) by using
      `DATABASE_CONNECTION_URL=jdbc:postgresql://localhost:5432/citybikeapp ./gradlew run --args "dataloader"`.
        * Windows terminals need some additional wizardry to set env vars: `SET FOO=bar` or `$env:FOO='bar'`
* Run application using Gradle task `run`, for example with `./gradlew run`
* Application API will be available under http://localhost:8080/
    * See [api.yml](gen/api.yml) or [https://mtuomiko.github.io/citybikeapp/](https://mtuomiko.github.io/citybikeapp/)
      for available endpoints

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

Application can be started in a single run data loading mode by providing the command line argument `dataloader`. In
this mode, the actual server will not be started and the application will exit after completion.

Data loader will read the provided configuration and download CSV files to batch insert their data to the database.
Loader will only delete the used local files when running in `prod` environment. This means that downloaded data can
remain in place, in containers also.

Loader will perform simple validation and cleaning on the data. Anything not matching the assumed format or data model,
will cause the entry to be ignored. Duplicate entries are ignored. For stations the primary key ID is pulled straight
from source CSV and subsequent INSERTs on same ID are ignored. For journeys the uniqueness in maintained by an all
column unique constraint/index, a bit doubtful about this... (temp table on insert could work also?)

Example for running data loader: `./gradlew run --args "dataloader"`

## Code generation and API generation

Backend DAO layer implementation depends on jOOQ codegeneration (data about tables and columns and so on). It needs
some schema to use for generation and here the schema is being formed in a temporary PostgreSQL testcontainer by running
Flyway migrations on it. Meaning the build doesn't depend on an already existing DB, but a docker host is required.

API specifications are being generated in [gen/api.yml](gen/api.yml) based on the controllers and API response classes.
The spec isn't too pretty, API-first approach would probably be nicer and result in more coherent spec file.

## Environment variables

This table describes relevant variables when running the application in production mode. Micronaut allows property
overriding using environment variables (like Spring does) but these are explicitly configured.

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

| Environment variable                  | Description                                             | Default                 | Example                                                   |
|---------------------------------------|---------------------------------------------------------|-------------------------|-----------------------------------------------------------|
| `CITYBIKEAPP_DATALOADER_MIN_DISTANCE` | Minimum filter on journey length (meters)               | `10`                    |                                                           |
| `CITYBIKEAPP_DATALOADER_MIN_DURATION` | Minimum filter on journey duration (seconds)            | `10`                    |                                                           |
| `CITYBIKEAPP_DATALOADER_BATCH_SIZE`   | Database batch size for inserts                         | `1000`                  |                                                           |
| `CITYBIKEAPP_DATALOADER_STATION_URL`  | URL for station data CSV file                           | [[1]](#default_station) | `http://foo.bar/file.csv`                                 |
| `CITYBIKEAPP_DATALOADER_JOURNEY_URLS` | Comma separated list of URLs for journey data CSV files | [[2]](#default_journey) | `http://foo.bar/journey1.csv,http://foo.bar/journey2.csv` |

<a id="default_station"></a>[1] `https://opendata.arcgis.com/datasets/726277c507ef4914b0aec3cbcfcbfafc_0.csv`

<a id="default_journey"></a>[2] `https://dev.hsl.fi/citybikes/od-trips-2021/2021-05.csv,https://dev.hsl.fi/citybikes/od-trips-2021/2021-06.csv,https://dev.hsl.fi/citybikes/od-trips-2021/2021-07.csv`

## Used technologies

* Framework: Micronaut
    * Wanted to see how these new AOT focused frameworks do. Original inspiration came from when I was running a
      different project's Spring Boot backend on a free deployment at Render.com, and it took 5 minutes to start the
      container. Obviously the free tier had very limited resources but still.
* Build/tooling: Gradle
    * Mostly familiar with this one vs maven
* Language: Kotlin
    * I just like it :)
* Database: PostgreSQL
* DB access: jOOQ
    * Allowed writing the statistic queries in a somewhat reasonable manner: Jdbi seemed too manual and JPA/Hibernate an
      overkill
* Migrations: Flyway
* Code quality / static analysis: detekt, Spotless
* Test coverage: JaCoCo

## TODOs

* Use single testcontainer for all the tests. Even though running the full application is fast (compared to Spring), the
  database container is adding some overhead.
* Enable strict checking for nullable query params when the option becomes available,
  see [micronaut issue 5135](https://github.com/micronaut-projects/micronaut-core/issues/5135)
* Switch to abstract controller/interface generation from OpenAPI specs when a suitable code generator supports Jakarta
  EE
  annotations. Annotation based API writing is getting
  ridiculous. [java-micronaut-server](https://github.com/OpenAPITools/openapi-generator/blob/master/docs/generators/java-micronaut-server.md)
  generator has the `useJakartaEe` setting, but it's not implemented.
