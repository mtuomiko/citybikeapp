import React from "react";
import { AspectRatio, Box } from "@chakra-ui/react";
import { StationDetails } from "generated";

/**
 * Based on embedded OpenStreetMaps iframe.
 * See https://wiki.openstreetmap.org/wiki/Using_OpenStreetMap#Maps_on_your_website
 */

const latitudeDeltaDegrees = 0.00118; // distance from center of map (station marker) to top and bottom of bounding box
const aspectRatio = 5 / 4;
const longitudeDeltaDegrees = aspectRatio * latitudeDeltaDegrees;
const osmUrlPrefix = "https://www.openstreetmap.org/export/embed.html?bbox=";

const StationMap = ({ stationDetails }: { stationDetails: StationDetails }) => {
  const latitude = stationDetails.latitude;
  const longitude = stationDetails.longitude;

  const minLongitude = longitude - longitudeDeltaDegrees;
  const minLatitude = latitude - latitudeDeltaDegrees;
  const maxLongitude = longitude + longitudeDeltaDegrees;
  const maxLatitude = latitude + latitudeDeltaDegrees;

  const boundingBoxString = `${minLongitude},${minLatitude},${maxLongitude},${maxLatitude}`;
  const mapSource = encodeURI(`${osmUrlPrefix}${boundingBoxString}&marker=${latitude},${longitude}`);

  return (
    <Box m="2">
      <AspectRatio ratio={aspectRatio} maxWidth="500px">
        <iframe
          src={mapSource}
          style={{ border: "1px solid black" }}
        ></iframe>
      </AspectRatio>
    </Box>
  );
};

export default StationMap;
