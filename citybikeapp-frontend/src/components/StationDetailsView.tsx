import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { Box, Card, CardBody, Divider, Flex, Heading, HStack, Text } from "@chakra-ui/react";
import { StationDetails, StationDetailsResponse } from "generated";
import { stationApi } from "clients";
import StationMap from "./StationMap";
import StationStatisticsView from "./StationStatisticsView";

const StationDetailsView = () => {
  const params = useParams();
  const stationId = Number(params.stationId);
  const [response, setResponse] = useState<StationDetailsResponse | undefined>(undefined);

  useEffect(() => {
    const getStation = async () => {
      if (Number.isNaN(stationId)) return;

      const response = await stationApi.getStationDetails(stationId);
      setResponse(response.data);
    };

    void getStation();
  }, [stationId]);

  if (response === undefined) return null;

  const details = (details: StationDetails) => (
    <Flex justify={"space-between"} flexWrap={"wrap"}>
      <Box>
        <Heading as="h5" size="xs">Station name</Heading>
        <HStack data-cy="station-names-container">
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
        <Divider />
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
        <Divider />
        {(details.cityFinnish !== "" || details.citySwedish !== "") &&
          <>
            <Heading as="h5" size="xs">City</Heading>
            <HStack>
              <Box>
                <Heading size="xs">Finnish</Heading>
                <Text>{details.cityFinnish}</Text>
              </Box>
              <Box>
                <Heading size="xs">Swedish</Heading>
                <Text>{details.citySwedish}</Text>
              </Box>
            </HStack>
          </>
        }
        <Heading size="xs">Bike capacity</Heading>
        <Text>{details.capacity} bikes</Text>
        {(details.operator !== "") &&
          <>
            <Heading size="xs">Operator</Heading>
            <Text>{details.operator}</Text>
          </>
        }
      </Box>
      <Box>
        <StationMap stationDetails={details} />
      </Box>
    </Flex>
  );

  return (
    <Card p={2}>
      <CardBody>
        {details(response.station)}
        <StationStatisticsView key={stationId} stationId={stationId} />
      </CardBody>
    </Card>
  );
};

export default StationDetailsView;
