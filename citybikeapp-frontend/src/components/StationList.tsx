import React, { ChangeEvent, useEffect, useMemo, useState } from "react";
import { Link as ReactRouterLink } from "react-router-dom";
import { TableContainer, Table, Text, Thead, Tr, Th, Tbody, Td, Link, Button, Box, Input, HStack } from "@chakra-ui/react";
import debounce from "lodash/debounce";
import { Station } from "generated";
import stationService from "service/station";

const StationList = () => {
  const [parameters, setParameters] = useState<{
    search: string
    page: number
  }>({
    search: "",
    page: 0
  });
  const [stations, setStations] = useState<Station[]>([]);
  const [moreAvailable, setMoreAvailable] = useState(false);

  useEffect(() => {
    const getStations = async () => {
      const response = await stationService.getStations({
        search: parameters.search,
        page: parameters.page,
        pageSize: undefined
      });

      if (parameters.page > 0) {
        setStations(current => current.concat(response.stations));
      } else {
        setStations(response.stations);
      }
      setMoreAvailable(response.moreAvailable);
    };

    void getStations();
  }, [parameters]);

  const handleSearchChange = (e: ChangeEvent<HTMLInputElement>) => {
    setParameters({
      search: e.target.value,
      page: 0
    });
  };

  const debouncedHandleSearchChange = useMemo(() => {
    return debounce(handleSearchChange, 1000);
  }, []);

  const handleFetchMore = async () => {
    setParameters(current => ({
      ...current,
      page: current.page + 1
    }));
  };

  return (
    <Box m="2">
      <HStack>
        <Text>Search</Text>
        <Input
          placeholder="Search stations based on name or address"
          onChange={debouncedHandleSearchChange}
        >
        </Input>
      </HStack>
      <TableContainer>
        <Table>
          <Thead>
            <Tr>
              <Th>Name</Th>
              <Th>Street address</Th>
              <Th>City</Th>
              <Th>Operator</Th>
              <Th>Capacity</Th>
            </Tr>
          </Thead>
          <Tbody data-cy="station-list-table-body">
            {stations.map(station => (
              <Tr key={station.id}>
                <Td>
                  <Link as={ReactRouterLink} to={`/stations/${station.id}`}>{station.nameFinnish}</Link>
                </Td>
                <Td>{station.addressFinnish}</Td>
                <Td>{station.cityFinnish}</Td>
                <Td>{station.operator}</Td>
                <Td>{station.capacity}</Td>
              </Tr>
            ))}
            {moreAvailable &&
              <Tr><Td colSpan={6}><Button onClick={handleFetchMore}>Load more</Button></Td></Tr>
            }
          </Tbody>
        </Table>
      </TableContainer>
    </Box>

  );
};

export default StationList;
