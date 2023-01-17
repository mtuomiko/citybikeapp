import { Configuration, JourneyApi, StationApi } from "generated";
import config from "config";

const configuration = new Configuration({
  basePath: config.apiBaseUrl
});

export const journeyApi = new JourneyApi(configuration);
export const stationApi = new StationApi(configuration);
