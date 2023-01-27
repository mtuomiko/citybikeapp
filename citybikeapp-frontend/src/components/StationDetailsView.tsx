import React, { useEffect, useState } from "react";
import { Link as ReactRouterLink, useParams } from "react-router-dom";
import { Box, Card, CardBody, CardHeader, Grid, GridItem, Heading, HStack, Link, Stat, StatLabel, StatNumber, Table, TableContainer, Tbody, Td, Text, Th, Thead, Tr } from "@chakra-ui/react";
import { StationStatistics, StationDetailsWithStatisticsResponse, TopStation, StationDetails } from "generated";
import { stationApi } from "clients";
import { useStationsLimited } from "contexts/StationsLimitedContext";
import { metersToKilometers } from "utils/unitConversion";
import StationMap from "./StationMap";

const StationDetailsView = () => {
  const params = useParams();
  const { state } = useStationsLimited();
  const stationId = Number(params.stationId);
  const [response, setResponse] = useState<StationDetailsWithStatisticsResponse | undefined>(undefined);
  useEffect(() => {
    const getStation = async () => {
      if (Number.isNaN(stationId)) return;

      const response = await stationApi.getStationWithStatistics(stationId);
      setResponse(response.data);
    };

    void getStation();
  }, [stationId]);

  if (response === undefined || state.stations.allIds.length === 0) return null;

  const statistics = (statistics: StationStatistics) => (
    <Card>
      <CardHeader><Heading as="h3" size="md">Statistics</Heading></CardHeader>
      <CardBody>
        <Grid templateColumns="repeat(4, 1fr)" templateRows="repeat(2, auto)" gap="4">
          <GridItem>
            <Stat>
              <StatLabel>Departing journeys</StatLabel>
              <StatNumber>{statistics.departureCount}</StatNumber>
            </Stat>
          </GridItem>
          <GridItem>
            <Stat>
              <StatLabel>Departing journey average distance</StatLabel>
              <StatNumber>{metersToKilometers(statistics.departureAverageDistance)} km</StatNumber>
            </Stat>
          </GridItem>
          <GridItem>
            <Stat>
              <StatLabel>Arriving journeys</StatLabel>
              <StatNumber>{statistics.arrivalCount}</StatNumber>
            </Stat>
          </GridItem>
          <GridItem>
            <Stat>
              <StatLabel>Arriving journey average distance</StatLabel>
              <StatNumber>{metersToKilometers(statistics.arrivalAverageDistance)} km</StatNumber>
            </Stat>
          </GridItem>
          <GridItem colSpan={2}>
            {topStations("Most popular stations where people go to", statistics.topStationsForDepartingTo)}
          </GridItem>
          <GridItem colSpan={2}>
            {topStations("Most popular stations where people arrive from", statistics.topStationsForArrivingHere)}
          </GridItem>
        </Grid>
      </CardBody>
    </Card>
  );

  const topStations = (name: string, stations: TopStation[]) => (

    <TableContainer>
      <Table>
        <Thead>
          <Tr>
            <Th>{name}</Th>
            <Th>Journeys</Th>
          </Tr>
        </Thead>
        <Tbody>
          {stations.map(station =>
            <Tr key={station.id}>
              <Td>
                <Link
                  as={ReactRouterLink}
                  to={`/stations/${station.id}`}
                >{state.stations.byId[station.id].nameFinnish}</Link>
              </Td>
              <Td>
                {station.journeyCount}
              </Td>
            </Tr>
          )}
        </Tbody>
      </Table>
    </TableContainer>
  );

  const details = (details: StationDetails) => (
    <Box>
      <Heading as="h5" size="xs">Station name</Heading>
      <HStack>
        <Box>
          <Heading size="xs">Finnish</Heading>
          <Text>{details.nameFinnish}</Text>
        </Box>
        <Box>
          <Heading size="xs">Swedish</Heading>
          <Text>{details.nameSwedish}</Text>
        </Box>
        <Box>
          <Heading size="xs">English</Heading>
          <Text>{details.nameEnglish}</Text>
        </Box>
      </HStack>
      <Heading as="h5" size="xs">Address</Heading>
      <HStack>
        <Box>
          <Heading size="xs">Finnish</Heading>
          <Text>{details.addressFinnish}</Text>
        </Box>
        <Box>
          <Heading size="xs">Swedish</Heading>
          <Text>{details.addressSwedish}</Text>
        </Box>
      </HStack>
      <Text>City Finnish: {details.cityFinnish}</Text>
      <Text>City Swedish: {details.citySwedish}</Text>
      <Heading size="xs">Bike capacity</Heading>
      <Text>{details.capacity} bikes</Text>
      <Text>Operator: {details.operator}</Text>
      <StationMap stationDetails={details} />
    </Box>
  );

  return (
    <Card m={2}>
      <CardBody>
        {details(response.station)}
        {statistics(response.statistics)}
      </CardBody>
    </Card>
  );
};

export default StationDetailsView;
