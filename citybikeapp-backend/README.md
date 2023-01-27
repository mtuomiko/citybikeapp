# City Bike App backend

Note that application can be used to automatically fetch and load data that is owned by City Bike Finland (journey data)
and Helsingin seudun liikenne (HSL) (station data). Data is licensed
under [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/) as of 2nd January 2023.

## Used technologies

* Framework: Micronaut
* Build/tooling: Gradle
* Language: Kotlin
* Database: PostgreSQL
* DB access: jOOQ
* Migrations: Flyway
* Code quality / static analysis: detekt, Spotless
* Test coverage: JaCoCo

## Getting started

Requirements

* Java JDK 17
* Docker host (for jOOQ code generation that runs against a temporary PostgreSQL container). podman might work
* PostgreSQL 14 database access (when running)

#### Running locally

Use the correct Gradle wrapper for your environment: `gradlew.bat` for Windows, `gradlew`
otherwise.

* App assumes an existing PostgreSQL 14 instance to be available at `postgresql://host.docker.internal:5432/citybikeapp`
  with credentials `postgres:Hunter2`. Run one for example with docker
  using `docker run -d --restart --name dev-postgres -p 5432:5432 -e POSTGRES_USER=postgres -e POSTGRES_DB=citybikeapp -e POSTGRES_PASSWORD=Hunter2 postgres:14`
* Run dataloading using Gradle task `run` with `dataloader` argument, for example
  with `./gradlew run --args "dataloader"`
* Run application using Gradle task `run`, for example with `./gradlew run`
* Application API will be available under http://localhost:8080/

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
