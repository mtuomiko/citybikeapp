import { Box, Button, Table, TableContainer, Tbody, Td, Th, Thead, Tr } from "@chakra-ui/react";
import React, { useEffect, useState } from "react";
import { TriangleDownIcon, TriangleUpIcon } from "@chakra-ui/icons";
import { useStationsLimited } from "contexts/StationsLimitedContext";
import { DirectionEnum } from "enums";
import journeyService, { OrderBy } from "service/journey";
import { Journey } from "types";
import JourneyRow from "./JourneyRow";

interface Header { displayTitle: string, orderBy?: OrderBy };
const headers: Header[] = [
  { displayTitle: "Departure time", orderBy: "departureAt" },
  { displayTitle: "Arrival time", orderBy: "arrivalAt" },
  { displayTitle: "Departure station" },
  { displayTitle: "Arrival station" },
  { displayTitle: "Distance (km)", orderBy: "distance" },
  { displayTitle: "Duration (min)", orderBy: "duration" },
];

const JourneyList = () => {
  const { state } = useStationsLimited();
  const [parameters, setParameters] = useState<{
    orderBy: OrderBy
    ascending: boolean
    pageSize: number
  }>({
    orderBy: "departureAt",
    ascending: false,
    pageSize: 25
  });
  const [cursor, setCursor] = useState<string | undefined>(undefined);
  const [journeys, setJourneys] = useState<Journey[]>([]);

  useEffect(() => {
    const getJourneys = async () => {
      const response = await journeyService.getJourneys({
        orderBy: parameters.orderBy,
        direction: (parameters.ascending) ? DirectionEnum.Ascending : DirectionEnum.Descending,
        pageSize: parameters.pageSize,
        cursor: undefined
      }
      );
      setJourneys(response.journeys);
      setCursor(response.cursor);
    };

    void getJourneys();
  }, [parameters]);

  const handleOrderByClick = (orderBy: OrderBy) => {
    if (parameters.orderBy === orderBy) {
      setParameters(parameters => ({ ...parameters, ascending: !parameters.ascending }));
    } else {
      setParameters(parameters => ({ ...parameters, orderBy }));
    }
  };

  const handleFetchMore = async () => {
    if (cursor !== undefined && cursor !== null && cursor.length > 0) {
      const response = await journeyService.getJourneys({
        orderBy: parameters.orderBy,
        direction: (parameters.ascending) ? DirectionEnum.Ascending : DirectionEnum.Descending,
        pageSize: parameters.pageSize,
        cursor
      });
      setJourneys(current => current.concat(response.journeys));
      setCursor(response.cursor);
    }
  };

  const createOrderByIcon = (orderBy?: OrderBy) => {
    if (orderBy === null || parameters.orderBy !== orderBy) return null;

    return (parameters.ascending)
      ? <TriangleUpIcon />
      : <TriangleDownIcon />;
  };

  const createColumnHeader = (header: Header) => {
    const orderBy = header.orderBy;
    const currentOrderByIcon = createOrderByIcon(orderBy);
    const iconProp = (currentOrderByIcon === null) ? {} : { rightIcon: currentOrderByIcon };
    const element = (orderBy !== undefined)
      ? <Button
        {...iconProp}
        onClick={() => { handleOrderByClick(orderBy); }}
      >{header.displayTitle}</Button>
      : <Box>{header.displayTitle}</Box>;
    return <Th key={header.displayTitle}>{element}</Th>;
  };

  const createTableHeaders = () => {
    return (
      <Thead>
        <Tr>
          {headers.map(header => createColumnHeader(header))}
        </Tr>
      </Thead>
    );
  };

  if (state.stations.allIds.length === 0) return <div>Still loading or no results</div>;

  return (
    <TableContainer>
      <Table>
        {createTableHeaders()}
        <Tbody>
          {journeys.map(journey => (
            <JourneyRow key={journey.id} journey={journey} />
          ))}
          {(cursor !== undefined) &&
            <Tr><Td colSpan={6}><Button onClick={handleFetchMore}>Load more</Button></Td></Tr>
          }
        </Tbody>
      </Table>
    </TableContainer>
  );
};

export default JourneyList;
