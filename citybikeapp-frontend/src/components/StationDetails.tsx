import React, { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { StationStatistics, StationWithStatistics, TopStation } from "generated";
import { stationApi } from "clients";

const StationDetails = () => {
  const params = useParams();
  const stationId = Number(params.stationId);
  const [station, setStation] = useState<StationWithStatistics | undefined>(undefined);
  useEffect(() => {
    const getStation = async () => {
      if (Number.isNaN(stationId)) return;

      const response = await stationApi.getStationWithStatistics(stationId);
      setStation(response.data);
    };

    void getStation();
  }, [stationId]);

  if (station === undefined) return null;

  const statistics = (statistics: StationStatistics) => (
    <>
      <h3>Statistics</h3>
      <ul>
        <li>departureCount: {statistics.departureCount}</li>
        <li>arrivalCount {statistics.arrivalCount}</li>
        <li>departureJourneyAverageDistance {statistics.departureJourneyAverageDistance}</li>
        <li>arrivalJourneyAverageDistance {statistics.arrivalJourneyAverageDistance}</li>
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
          <span><Link to={`/station/${station.id}`}>{station.nameFinnish}</Link>, {station.journeyCount} journeys</span>
        </div>
      )}
    </div >
  );

  return (
    <div>
      <ul>
        <li>id: {station.id}</li>
        <li>nameFinnish: {station.nameFinnish}</li>
        <li>nameSwedish: {station.nameEnglish}</li>
        <li>nameEnglish: {station.nameEnglish}</li>
        <li>addressFinnish: {station.addressFinnish}</li>
        <li>addressSwedish: {station.addressSwedish}</li>
        <li>cityFinnish: {station.cityFinnish}</li>
        <li>citySwedish: {station.citySwedish}</li>
        <li>operator: {station.operator}</li>
        <li>capacity: {station.capacity}</li>
        <li>longitude: {station.longitude}</li>
        <li>latitude: {station.latitude}</li>
      </ul>
      {statistics(station.statistics)}
    </div>
  );
};

export default StationDetails;
