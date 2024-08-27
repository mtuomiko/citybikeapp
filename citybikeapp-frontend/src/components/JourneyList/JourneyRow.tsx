import { Text, Link, Td, Tooltip, Tr } from "@chakra-ui/react";
import React, { } from "react";
import { Link as ReactRouterLink } from "react-router-dom";
import { useStationsLimited } from "contexts/StationsLimitedContext";
import { Journey } from "types";
import { metersToKilometers } from "utils/unitConversion";

const JourneyRow = ({ journey }: { journey: Journey }) => {
  const { state } = useStationsLimited();

  const displayTime = (time: Date) => time.toLocaleString("fi-FI", { dateStyle: "short", timeStyle: "short" });

  const createTimeCell = (time: Date) => (
    <Td>
      <Tooltip label={<Text fontSize={"xs"}>{time.toString()}</Text>} openDelay={1000}>
        {displayTime(time)}
      </Tooltip>
    </Td>
  );

  // Round down unless result would be zero minutes
  const secondsToMinutes = (seconds: number) => {
    const minutes = Math.floor(seconds / 60);
    return (minutes === 0) ? 1 : minutes;
  };

  return (
    <Tr key={journey.id}>
      {createTimeCell(journey.departureAt)}
      {createTimeCell(journey.arrivalAt)}
      <Td>
        <Link
          as={ReactRouterLink}
          to={`/stations/${journey.departureStationId}`}
        >{state.stations.byId[journey.departureStationId].nameFinnish}</Link>
      </Td>
      <Td>
        <Link
          as={ReactRouterLink}
          to={`/stations/${journey.arrivalStationId}`}
        >{state.stations.byId[journey.arrivalStationId].nameFinnish}</Link>
      </Td>
      <Td>{metersToKilometers(journey.distance)} km</Td>
      <Td>{secondsToMinutes(journey.duration)} min</Td>
    </Tr>
  );
};

export default JourneyRow;
