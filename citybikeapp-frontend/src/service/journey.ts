import { journeyApi } from "clients";
import { Direction } from "enums";
import { Journey as APIJourney } from "generated";
import { Journey } from "types";

export type OrderBy = "departureAt" | "arrivalAt" | "distance" | "duration";

export interface JourneyParameters {
  orderBy: OrderBy
  direction: Direction
  pageSize: number
  cursor: string | undefined
}

const parseJourney = (journey: APIJourney): Journey => {
  const parsedJourney = {
    ...journey,
    departureAt: new Date(journey.departureAt),
    arrivalAt: new Date(journey.arrivalAt)
  };
  return parsedJourney;
};

interface JourneysResponse {
  journeys: Journey[]
  cursor: string | undefined
}

const getJourneys = async (parameters: JourneyParameters): Promise<JourneysResponse> => {
  const response = await journeyApi.getJourneys(
    parameters.orderBy,
    parameters.direction,
    parameters.pageSize,
    parameters.cursor
  );

  // map axios journeys to our own with more detailed typing and parsed timestamps
  const journeys = response.data.journeys.map(journey => parseJourney(journey));

  const cursor = response.data.meta.nextCursor ?? undefined;
  return {
    journeys,
    cursor
  };
};

export default { getJourneys };
