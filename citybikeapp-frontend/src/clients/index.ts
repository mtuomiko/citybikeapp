import { Configuration, JourneyApi, StationApi } from "generated";
import config from "config";

/**
 * Create axios compatible config using global base URL for the backend, and export clients/apis that use that config.
 */

const configuration = new Configuration({
  basePath: config.apiBaseUrl
});

export const journeyApi = new JourneyApi(configuration);
export const stationApi = new StationApi(configuration);
