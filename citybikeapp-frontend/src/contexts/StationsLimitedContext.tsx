import React, { createContext, useContext, useReducer } from "react";
import { StationLimited } from "generated";
import { ID } from "types";
import { stationApi } from "clients";

interface Action { type: "setAll", payload: StationLimited[] }
type Dispatch = (action: Action) => void;
interface State {
  stations: {
    byId: Record<ID, StationLimited>
    allIds: ID[]
  }
};
interface StationsLimitedProviderProps { children: React.ReactNode };

const StationsLimitedContext = createContext<
  { state: State, dispatch: Dispatch } | undefined
>(undefined);

const stationsReducer = (state: State, action: Action): State => {
  switch (action.type) {
  case "setAll": {
    const allStations = action.payload.reduce(
      (memo, station) => ({ ...memo, [station.id]: { ...station } }),
      {}
    );
    const allIds = Object.keys(allStations);
    return { stations: { byId: allStations, allIds } };
  }
  }
};

const StationsLimitedProvider = ({ children }: StationsLimitedProviderProps) => {
  const [state, dispatch] = useReducer(stationsReducer, { stations: { byId: {}, allIds: [] } });
  const value = { state, dispatch };

  return (
    <StationsLimitedContext.Provider value={value}>
      {children}
    </StationsLimitedContext.Provider>

  );
};

const useStationsLimited = () => {
  const context = useContext(StationsLimitedContext);
  if (context === undefined) {
    throw new Error("useStationsLimited must be used within a StationsLimitedProvider");
  }
  return context;
};

const getAll = async (dispatch: React.Dispatch<Action>) => {
  const response = await stationApi.getAllStationsLimited();
  dispatch({ type: "setAll", payload: response.data.stations });
};

export { StationsLimitedProvider, useStationsLimited, getAll };
