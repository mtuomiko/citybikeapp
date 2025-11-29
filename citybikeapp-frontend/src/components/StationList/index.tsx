import React, { ChangeEvent, useEffect, useMemo, useState } from "react";
import { Link as ReactRouterLink } from "react-router-dom";
import { TableContainer, Table, Text, Thead, Tr, Th, Tbody, Td, Link, Button, Box, Input, HStack } from "@chakra-ui/react";
import debounce from "lodash/debounce";
import { TriangleDownIcon, TriangleUpIcon } from "@chakra-ui/icons";
import { Station } from "generated";
import stationService, { StationOrderBy } from "service/station";
import { DirectionEnum } from "enums";

interface Header { displayTitle: string, orderBy?: StationOrderBy };
const headers: Header[] = [
  { displayTitle: "Name", orderBy: "nameFinnish" },
  { displayTitle: "Street address", orderBy: "addressFinnish" },
  { displayTitle: "City", orderBy: "cityFinnish" },
  { displayTitle: "Operator", orderBy: "operator" },
  { displayTitle: "Capacity", orderBy: "capacity" }
];

const StationList = () => {
  const [parameters, setParameters] = useState<{
    orderBy: StationOrderBy | null
    ascending: boolean | null
    search: string
    page: number
  }>({
    orderBy: null,
    ascending: null,
    search: "",
    page: 0
  });
  const [stations, setStations] = useState<Station[]>([]);
  const [moreAvailable, setMoreAvailable] = useState(false);

  useEffect(() => {
    const getStations = async () => {
      const response = await stationService.getStations({
        orderBy: parameters.orderBy,
        direction: (parameters.ascending === true) ? DirectionEnum.Ascending : DirectionEnum.Descending,
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
    setParameters((prevValue) => {
      return {
        orderBy: prevValue.orderBy,
        ascending: prevValue.ascending,
        search: e.target.value,
        page: 0
      };
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

  const handleOrderByClick = (orderBy: StationOrderBy) => {
    // If current order by is the same, toggle ascending/descending. Always reset to page 0 on new order by.
    if (parameters.orderBy === orderBy) {
      setParameters(parameters => ({ ...parameters, ascending: !(parameters.ascending ?? false), page: 0 }));
    } else {
      setParameters(parameters => ({ ...parameters, orderBy, page: 0 }));
    }
  };

  const createOrderByIcon = (orderBy?: StationOrderBy) => {
    if (orderBy === null || parameters.orderBy !== orderBy) return null;

    return (parameters.ascending === true)
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
          {createTableHeaders()}
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
