import React from "react";
import { Link } from "react-router-dom";
import { useStationsLimited } from "contexts/StationsLimitedContext";

const StationList = () => {
  const { state } = useStationsLimited();

  return (
    <div>
      <ul>
        {state.stations.allIds.map(stationId => {
          const station = state.stations.byId[stationId];
          return <div key={stationId}>
            <span><Link to={`/stations/${station.id}`}>{station.nameFinnish}</Link></span>
          </div>;
        })}
      </ul>
    </div>
  );
};

export default StationList;
