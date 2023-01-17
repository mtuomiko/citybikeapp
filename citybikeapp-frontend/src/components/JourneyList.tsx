import { Button, Link, Table, Tbody, Td, Th, Thead, Tr } from "@chakra-ui/react";
import React, { useEffect, useState } from "react";
import { Link as ReactRouterLink } from "react-router-dom";
import { useStationsLimited } from "contexts/StationsLimitedContext";
import { Direction, DirectionEnum } from "enums";
import journeyService, { OrderBy } from "service/journey";
import { Journey } from "types";

const JourneyList = () => {
  const { state } = useStationsLimited();
  const [parameters] = useState<{
    orderBy: OrderBy
    direction: Direction
    pageSize: number
  }>({
    orderBy: "departureAt",
    direction: DirectionEnum.Descending,
    pageSize: 25
  });
  const [cursor, setCursor] = useState<string | undefined>(undefined);
  const [journeys, setJourneys] = useState<Journey[]>([]);

  useEffect(() => {
    const getStation = async () => {
      const response = await journeyService.getJourneys({
        orderBy: parameters.orderBy,
        direction: parameters.direction,
        pageSize: parameters.pageSize,
        cursor
      }
      );
      setJourneys(response.journeys);
      setCursor(response.cursor);
    };

    void getStation();
  }, [parameters]);

  const handleFetchMore = async () => {
    if (cursor !== undefined && cursor !== null && cursor.length > 0) {
      const response = await journeyService.getJourneys({
        orderBy: parameters.orderBy,
        direction: parameters.direction,
        pageSize: parameters.pageSize,
        cursor
      });
      setJourneys(current => current.concat(response.journeys));
      setCursor(response.cursor);
    }
  };

  if (state.stations.allIds.length === 0) return <div>Loading</div>;

  return (
    <div>
      <Table>
        <Thead>
          <Tr>
            <Th>Departure time</Th>
            <Th>Arrival time</Th>
            <Th>Departure station</Th>
            <Th>Arrival station</Th>
            <Th>Distance (km)</Th>
            <Th>Duration (min.)</Th>
          </Tr>
        </Thead>
        <Tbody>
          {journeys.map(journey => (
            <Tr key={journey.id}>
              <Td>{journey.departureAt.toLocaleString("fi-FI")}</Td>
              <Td>{journey.arrivalAt.toLocaleString("fi-FI")}</Td>
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
              <Td>{journey.distance}</Td>
              <Td>{journey.duration}</Td>
            </Tr>
          ))}
          {(cursor !== undefined) &&
            <Tr><Td colSpan={6}><Button onClick={handleFetchMore}>Load more</Button></Td></Tr>
          }
        </Tbody>
      </Table>
    </div>
  );
};

export default JourneyList;
