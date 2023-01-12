import React, { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { StationStatistics, StationDetailsWithStatisticsResponse, TopStation, StationDetails } from "generated";
import { stationApi } from "clients";

const StationDetailsView = () => {
  const params = useParams();
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

  if (response === undefined) return null;

  const statistics = (statistics: StationStatistics) => (
    <>
      <h3>Statistics</h3>
      <ul>
        <li>departureCount: {statistics.departureCount}</li>
        <li>arrivalCount {statistics.arrivalCount}</li>
        <li>departureJourneyAverageDistance {statistics.departureAverageDistance}</li>
        <li>arrivalJourneyAverageDistance {statistics.arrivalAverageDistance}</li>
      </ul>
      {topStations("Most popular stations where people arrive from", statistics.topStationsForArrivingHere)}
      {topStations("Most popular stations where people go to", statistics.topStationsForDepartingTo)}
    </>
  );

  const topStations = (name: string, stations: TopStation[]) => (
    <div>
      <h3>{name}</h3>
      {stations.map(station =>
        <div key={station.id}>
          <span><Link to={`/stations/${station.id}`}>{station.id}</Link>, {station.journeyCount} journeys</span>
        </div>
      )}
    </div >
  );

  const details = (details: StationDetails) => (
    <ul>
      <li>id: {details.id}</li>
      <li>nameFinnish: {details.nameFinnish}</li>
      <li>nameSwedish: {details.nameEnglish}</li>
      <li>nameEnglish: {details.nameEnglish}</li>
      <li>addressFinnish: {details.addressFinnish}</li>
      <li>addressSwedish: {details.addressSwedish}</li>
      <li>cityFinnish: {details.cityFinnish}</li>
      <li>citySwedish: {details.citySwedish}</li>
      <li>operator: {details.operator}</li>
      <li>capacity: {details.capacity}</li>
      <li>longitude: {details.longitude}</li>
      <li>latitude: {details.latitude}</li>
    </ul>
  );

  return (
    <div>
      {details(response.station)}
      {statistics(response.statistics)}
    </div>
  );
};

export default StationDetailsView;
