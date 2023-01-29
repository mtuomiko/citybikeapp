import { Journey as APIJourney, Station as APIStation } from "generated";

export type ID = string;

export type Journey = Omit<APIJourney, "id" | "departureAt" | "arrivalAt"> & {
  id: ID
  departureAt: Date
  arrivalAt: Date
};

export type Station = Omit<APIStation, "id"> & {
  id: ID
};
