import { stationApi } from "clients";
import { Station } from "generated";

export interface StationParameters {
  search: string
  page: number
  pageSize?: number
}

interface StationsResponse {
  stations: Station[]
  moreAvailable: boolean
}

const getStations = async (parameters: StationParameters): Promise<StationsResponse> => {
  const search = (parameters.search === "")
    ? undefined
    : parameters.search.replace(" ", "+");
  const response = await stationApi.getStations(search, parameters.page, undefined); // don't customize pageSize

  const stations = response.data.stations;
  const moreAvailable = response.data.meta.totalPages > (parameters.page + 1);
  return {
    stations,
    moreAvailable
  };
};

export default { getStations };
