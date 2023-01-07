import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { StationsLimitedResponse } from "generated";
import { stationApi } from "clients";

const StationList = () => {
  const [response, setResponse] = useState<StationsLimitedResponse | undefined>(undefined);
  useEffect(() => {
    const getStation = async () => {
      const response = await stationApi.getAllStationsLimited();
      setResponse(response.data);
    };

    void getStation();
  }, []);

  if (response === undefined) return null;

  return (
    <div>
      <ul>
        {response.stations.map(stationLimited =>
          <div key={stationLimited.id}>
            <span><Link to={`/stations/${stationLimited.id}`}>{stationLimited.nameFinnish}</Link></span>
          </div>)}
      </ul>
    </div>
  );
};

export default StationList;
