import React from "react";
import { createRoot } from "react-dom/client";
import { createBrowserRouter, RouterProvider } from "react-router-dom";
import App from "App";
import StationDetailsView from "components/StationDetailsView";
import StationList from "components/StationList";

const router = createBrowserRouter([
  {
    path: "/",
    element: <App />,
    children: [
      {
        path: "stations",
        element: <StationList />
      },
      {
        path: "stations/:stationId",
        element: <StationDetailsView />
      }
    ]
  },
]);

const root = createRoot(document.getElementById("root") as HTMLElement);
root.render(
  <React.StrictMode>
    <RouterProvider router={router} />
  </React.StrictMode>
);
