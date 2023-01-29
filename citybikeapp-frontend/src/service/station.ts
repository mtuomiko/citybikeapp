import dayjs, { extend } from "dayjs";
import utc from "dayjs/plugin/utc";
import { stationApi } from "clients";
import { Station, StationStatistics } from "generated";

extend(utc);

export interface StationParameters {
  search: string
  page: number
  pageSize?: number
}

interface StationsResponse {
  stations: Station[]
  moreAvailable: boolean
}

export interface StatisticsParameters {
  id: number
  from: Date | null
  to: Date | null
}

const getStations = async (parameters: StationParameters): Promise<StationsResponse> => {
  const search = (parameters.search === "")
    ? undefined
    : parameters.search.replace(" ", "+"); // API expects + separated strings
  const response = await stationApi.getStations(search, parameters.page, undefined); // don't customize pageSize

  const stations = response.data.stations;
  const moreAvailable = response.data.meta.totalPages > (parameters.page + 1);
  return {
    stations,
    moreAvailable
  };
};

const localDateTimeFormat = "YYYY-MM-DDTHH:mm:ss";

/**
 * Force timestamps initiated by browser/react-timepicker to UTC so we can finally pass them as local date times to
 * backend.
 */
const getStatistics = async (parameters: StatisticsParameters): Promise<StationStatistics> => {
  const from = (parameters.from === null)
    ? undefined
    : dayjs(parameters.from).utc(true).format(localDateTimeFormat);

  const to = (parameters.to === null)
    ? undefined
    : dayjs(parameters.to).utc(true).endOf("month").format(localDateTimeFormat);

  const response = await stationApi.getStationStatistics(parameters.id, from, to);
  return response.data.statistics;
};

export default { getStations, getStatistics };
