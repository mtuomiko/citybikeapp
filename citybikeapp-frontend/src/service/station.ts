import dayjs, { extend } from "dayjs";
import utc from "dayjs/plugin/utc";
import timezone from "dayjs/plugin/timezone";
import { stationApi } from "clients";
import { Direction, Station, StationStatistics } from "generated";

extend(utc);
extend(timezone);

export type StationOrderBy = "nameFinnish" | "addressFinnish" | "cityFinnish" | "operator" | "capacity";

export interface StationParameters {
  orderBy: StationOrderBy | null
  direction: Direction | null
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
  const orderBy = parameters.orderBy ?? undefined;
  const sort = parameters.direction ?? undefined;
  const search = (parameters.search === "")
    ? undefined
    : parameters.search.replace(/\s+/g, "+"); // API expects + separated strings
  const response = await stationApi.getStations(orderBy, sort, search, parameters.page, undefined); // don't customize pageSize

  const stations = response.data.stations;
  const moreAvailable = response.data.meta.totalPages > (parameters.page + 1);
  return {
    stations,
    moreAvailable
  };
};

/**
 * Convert date values to UTC (zero) offset ISO8601 strings for backend API. Could use offset but nothing gained, at
 * least now that all data is in a single timezone.
 *
 * MonthPicker as the source of dates can use whatever locale/timezone (browser default?). Convert those explicitly to
 * the same "time" but in Helsinki TZ.
 */
const getStatistics = async (parameters: StatisticsParameters): Promise<StationStatistics> => {
  const from = (parameters.from === null)
    ? undefined
    : dayjs.tz(parameters.from, "Europe/Helsinki").toISOString();

  // MonthPicker represents the selected month as a Date at the beginning of the selected month, we also need to convert
  // that to the end of the month.
  const to = (parameters.to === null)
    ? undefined
    : dayjs.tz(parameters.to, "Europe/Helsinki").endOf("month").toISOString();

  const response = await stationApi.getStationStatistics(parameters.id, from, to);
  return response.data.statistics;
};

export default { getStations, getStatistics };
