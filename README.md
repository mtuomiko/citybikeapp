# City Bike App

This is a monorepository for implementing the [pre-assignment for Solita Dev Academy Finland 2023](https://github.com/solita/dev-academy-2023-exercise).
It contains a React frontend & Micronaut backend based web application for displaying data about rental bike journeys and stations in Helsinki region.

See [Functionality](#functionality) for a list of original requirements and currently implemented functions.

Note that application can be used to automatically fetch and load data that is owned by City Bike Finland (journey data)
and Helsingin seudun liikenne (HSL) (station data). Data is licensed under [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/) as of 2nd January 2023.

## Running the application

If you just want to run the whole application, see Docker Compose instructions below. Otherwise 
see [citybikeapp-frontend](citybikeapp-frontend/) and [citybikeapp-backend](citybikeapp-backend/) folders for more info.

### With Docker Compose

This requires Java JDK 21, a Docker host, Docker Compose and local port `8080` to be available on the host machine.
You can change the port in [compose.yml](compose.yml).

1. Build backend
    * Windows: run `.\citybikeapp-backend\gradlew.bat -p .\citybikeapp-backend\ build` at project root
    * Other: run `./citybikeapp-backend/gradlew -p ./citybikeapp-backend build` at project root
2. Run the local docker compose setup.
    * Run `docker compose --env-file docker/.env.local --profile dataloader up` at project 
      root
    * Wait for the station and journey data loading to complete, or don't, the application should be available before 
      the loading completes.
3. City Bike App is found at [http://localhost:8080](http://localhost:8080) 

You can pull down the compose setup with `docker compose down`

## Functionality

### Data import

#### Recommended

* Import data from the CSV files to a database or in-memory storage ✅
* Validate data before importing ✅
* Don't import journeys that lasted for less than ten seconds ✅
* Don't import journeys that covered distances shorter than 10 meters ✅

### Journey list view

#### Recommended

* List journeys ✅
* For each journey show departure and return stations, covered distance in kilometers and duration in minutes ✅

#### Additional

* Pagination ✅
* Ordering per column ✅
* ~~Searching~~ 
* ~~Filtering~~

### Station list

#### Recommended

* List all the stations ✅

#### Additional

* Pagination ✅
* Searching ✅

### Single station view

#### Recommended

* Station name ✅
* Station address ✅
* Total number of journeys starting from the station ✅
* Total number of journeys ending at the station ✅

#### Additional
* Station location on the map ✅
* The average distance of a journey starting from the station ✅
* The average distance of a journey ending at the station ✅
* Top 5 most popular return stations for journeys starting from the station ✅
* Top 5 most popular departure stations for journeys ending at the station ✅
* Ability to filter all the calculations per month ✅

### Surprise us with

* ~~Endpoints to store new journeys data or new bicycle stations~~
* Running backend in Docker ✅
  * With a local Docker Compose setup for whole project
* ~~Running backend in Cloud~~
* Implement E2E tests ✅
  * Limited tests (see [e2e folder](e2e/)) but the setup exists and is being used on Github Actions. Gives some 
    guarantee that not everything is broken.
* ~~Create UI for adding journeys or bicycle stations~~

### Extra

* Pipeline generated Swagger docs about the backend API. Hosted by Github Pages ✅
  * Available at [https://mtuomiko.github.io/citybikeapp/](https://mtuomiko.github.io/citybikeapp/)
* API client generation for frontend using openapi-generator ✅
