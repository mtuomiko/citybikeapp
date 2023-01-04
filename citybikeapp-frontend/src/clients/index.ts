import { Configuration, StationApi } from "generated";
import config from "config";

const configuration = new Configuration({
  basePath: config.apiBaseUrl
});

export const stationApi = new StationApi(configuration);
