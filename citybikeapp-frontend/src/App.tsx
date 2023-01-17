import { useDisclosure } from "@chakra-ui/react";
import React, { useEffect } from "react";
import { Outlet } from "react-router-dom";
import Navbar from "components/Navbar";
import { useStationsLimited, getAll } from "contexts/StationsLimitedContext";

let stationsInitialized = false;

const App = () => {
  const { isOpen, onToggle } = useDisclosure();
  const { dispatch } = useStationsLimited();

  useEffect(() => {
    if (!stationsInitialized) {
      stationsInitialized = true;
      void getAll(dispatch);
    }
  }, []);

  return (
    <div>
      <Navbar isMobileOpen={isOpen} onToggle={onToggle} />
      <Outlet />
    </div>
  );
};

export default App;
