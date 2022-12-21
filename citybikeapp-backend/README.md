# City Bike App backend

## Data

Application can be used to automatically fetch and load data that is owned by City Bike Finland (journey data) and
Helsingin seudun liikenne (HSL) (station data). Data is licensed
under [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/) as of 2nd January 2023.

## Data loader

Application can be started in a single run data loading mode by providing the command line argument `dataloader`. In
this mode, the actual server will not be started and the application will exit after completion.

Data loader will read the provided configuration and download CSV files to batch insert their data to the database.
Loader will only delete used local files when running in `prod` environment. This means that downloaded data can remain
in place,
containers also.

Loader will perform simple validation and cleaning on the data. Anything not matching the assumed format or data model
will cause the entry to be ignored. Duplicate entries are ignored. For stations the primary key ID is pulled straight
from source CSV and subsequent INSERTs on same ID is ignored. For journeys the uniqueness in maintained by an all column
unique constraint/index, a bit doubtful about this... (temp table on insert could work also?)

Example for running data loader: `./gradlew run --args "dataloader"`

### Environment variables

This table describes relevant variables when running the final application in production mode. Micronaut allows property
overriding using environment variables (like Spring does) but these are explicitly configured.

| Environment variable                 | Description                                     | Default                                                   | Required | Example                                                   |
|--------------------------------------|-------------------------------------------------|-----------------------------------------------------------|----------|-----------------------------------------------------------|
| `PORT`                               | Server port                                     | `8080`                                                    |          |                                                           |
| `DATABASE_CONNECTION_URL`            | JDBC connection URL                             | `jdbc:postgresql://host.docker.internal:5432/citybikeapp` |          | `jdbc:postgresql://foo.bar:5432/packlister`               |
| `DATABASE_CONNECTION_USERNAME`       | DB username                                     | `postgres`                                                |          | `foo`                                                     |
| `DATABASE_CONNECTION_PASSWORD`       | DB password                                     | `Hunter2`                                                 |          | `bar`                                                     |
| `CITYBIKEAPP_DATALOADER_STATIONURL`  | URL for station data CSV file                   | [[1]](#default_station)                                   |          | `http://foo.bar/file.csv`                                 |
| `CITYBIKEAPP_DATALOADER_JOURNEYURLS` | Comma separated URLs for journey data CSV files | [[2]](#default_journey)                                   |          | `http://foo.bar/journey1.csv,http://foo.bar/journey2.csv` |

<a id="default_station"></a>[1] `https://opendata.arcgis.com/datasets/726277c507ef4914b0aec3cbcfcbfafc_0.csv`

<a id="default_journey"></a>[2] `https://dev.hsl.fi/citybikes/od-trips-2021/2021-05.csv,https://dev.hsl.fi/citybikes/od-trips-2021/2021-06.csv,https://dev.hsl.fi/citybikes/od-trips-2021/2021-07.csv`

#### TODOs

* Use single testcontainer for all the tests. Even though running the full application is fast (compared to Spring), the
  database container is adding some overhead.
* Mapping is getting verbose, use MapStruct?
