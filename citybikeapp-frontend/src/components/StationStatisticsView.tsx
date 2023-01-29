import React, { useEffect, useState } from "react";
import { Link as ReactRouterLink } from "react-router-dom";
import { Box, Button, Card, CardBody, CardHeader, Flex, Grid, GridItem, Heading, HStack, Link, Spacer, Stat, StatLabel, StatNumber, Table, TableContainer, Tbody, Td, Text, Th, Thead, Tr } from "@chakra-ui/react";
import { TopStation, StationStatistics } from "generated";
import { useStationsLimited } from "contexts/StationsLimitedContext";
import { metersToKilometers } from "utils/unitConversion";
import stationService from "service/station";
import MonthPicker from "./MonthPicker";

const StationStatisticsView = ({ stationId }: { stationId: number }) => {
  const { state } = useStationsLimited();
  const [statistics, setStatistics] = useState<StationStatistics | undefined>(undefined);
  const [fromDate, setFromDate] = useState<Date | null>(null);
  const [toDate, setToDate] = useState<Date | null>(null);

  useEffect(() => {
    const getStation = async () => {
      if (Number.isNaN(stationId)) return;

      const response = await stationService.getStatistics({ id: stationId, from: null, to: null });
      setStatistics(response);
    };

    void getStation();
  }, []);

  const handleStatisticsRefresh = async () => {
    const response = await stationService.getStatistics({ id: stationId, from: fromDate, to: toDate });

    setStatistics(response);
  };

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

  if (statistics === undefined || state.stations.allIds.length === 0) return null;

  return (
    <Card mt="2">
      <CardHeader>
        <Flex>
          <Heading as="h3" size="md">Statistics</Heading>
          <Spacer />
          <Box>
            <HStack>
              <Text>From</Text>
              <MonthPicker
                date={fromDate}
                onChange={setFromDate}
                placeholder="Select earliest month"
              />
              <Text>To</Text>
              <MonthPicker
                date={toDate}
                onChange={setToDate}
                placeholder="Select latest month"
              />
              <Button onClick={handleStatisticsRefresh} size="sm">Update statistics</Button>
            </HStack>
          </Box>
        </Flex>
      </CardHeader>
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
};

export default StationStatisticsView;
