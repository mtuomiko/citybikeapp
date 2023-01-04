import React from "react";
import { Outlet } from "react-router-dom";

const App = () => {
  return (
    <div>
      <p>Root App works!</p>
      <Outlet />
    </div>
  );
};

export default App;
