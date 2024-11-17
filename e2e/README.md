# City Bike App end-to-end tests

## Getting started

Requirements
* Node/npm. Developed on Node v18 so should work at least on that.

Running manually
* Install dependencies `npm ci`
* Start frontend `npm start` in frontend folder
* Copy `e2e/backend_data` content to `citybikeapp-backend/temp`
* Run backend in e2e dataloader mode with following env vars
  * Set env vars:

    ```
    SPRING_PROFILES_ACTIVE: dataloader
    CITYBIKEAPP_DATALOADER_STATIONURL: 'e2e/stations.csv'
    CITYBIKEAPP_DATALOADER_JOURNEYURLS: 'e2e/journeys.csv'
    ```
  * Run `./gradlew bootRun` in backend folder
* Run backend without the above env vars: `./gradlew bootRun` in backend folder
* Run tests with `npm run test:e2e` or open the Cypress GUI with `cy:open`

## NPM commands

* `test:e2e` Run in headless mode, for example, in pipelines
* `cy:open` Shorthand for opening the Cypress GUI
