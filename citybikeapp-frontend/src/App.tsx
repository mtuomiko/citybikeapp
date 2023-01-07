import React from "react";
import { Link, Outlet } from "react-router-dom";

const App = () => {
  return (
    <div>
      <p>Root App works!</p>
      <Link to={"/stations"}>Stations list</Link>
      <Outlet />
    </div>
  );
};

export default App;
