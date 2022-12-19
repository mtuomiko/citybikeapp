# City Bike App backend

## Data loader

Application can be started in a single run data loading mode by providing the command line argument `dataloader`. In
this mode, the actual server will not be started and the application will exit after completion.

Data loader will read the provided configuration and download CSV files to batch insert their data to the database.
Loader doesn't currently delete loaded files which is nice for development. Note that downloaded data will remain in
containers also.

Loader will perform simple validation and cleaning on the data. Anything not matching the assumed format or data model
will cause the entry to be ignored. Duplicate entries are ignored. For stations the primary key ID is pulled straight
from source CSV and subsequent INSERTs on same ID is ignored. For journeys the uniqueness in maintained by an all column
unique constraint/index, a bit doubtful about this...

Example for running data loader: `./gradlew run --args "dataloader"`

### Environment variables

This table describes relevant variables when running the final application in production mode. Micronaut framework will
handle others as well but these are explicitly configured.

| Environment variable                 | Description                                     | Default | Required | Example                                                   |
|--------------------------------------|-------------------------------------------------|--------:|----------|-----------------------------------------------------------|
| `PORT`                               | Server port                                     |  `8080` |          |                                                           |
| `DATABASE_CONNECTION_URL`            | JDBC connection URL                             |         | ✓        | `jdbc:postgresql://host.docker.internal:5432/packlister`  |
| `DATABASE_CONNECTION_USERNAME`       | DB username                                     |         | ✓        | `postgres`                                                |
| `DATABASE_CONNECTION_PASSWORD`       | DB password                                     |         | ✓        | `Hunter2`                                                 |
| `CITYBIKEAPP_DATALOADER_STATIONURL`  | URL for station data CSV file                   |         | ✓        | `http://foo.bar/file.csv`                                 |
| `CITYBIKEAPP_DATALOADER_JOURNEYURLS` | Comma separated URLs for journey data CSV files |         | ✓        | `http://foo.bar/journey1.csv,http://foo.bar/journey2.csv` |
