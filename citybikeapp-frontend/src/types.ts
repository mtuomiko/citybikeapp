import { Journey as APIJourney } from "generated";

export type ID = string;

type JourneyWithoutTimestampStrings = Omit<APIJourney, "departureAt" | "arrivalAt">;

export type Journey = JourneyWithoutTimestampStrings & {
  departureAt: Date
  arrivalAt: Date
};
