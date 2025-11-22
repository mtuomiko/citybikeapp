import React from "react";
import { createRoot } from "react-dom/client";
import { createBrowserRouter, RouterProvider } from "react-router-dom";
import { ChakraProvider, extendTheme } from "@chakra-ui/react";
import App from "App";
import StationDetailsView from "components/StationDetailsView";
import JourneyList from "components/JourneyList";
import { StationsLimitedProvider } from "contexts/StationsLimitedContext";
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
      },
      {
        path: "journeys",
        element: <JourneyList />
      }
    ]
  },
]);

const theme = extendTheme({
  components: {
    Link: {
      baseStyle: {
        fontWeight: "semibold"
      }
    }
  }
});

const root = createRoot(document.getElementById("root") as HTMLElement);
root.render(
  <React.StrictMode>
    <ChakraProvider theme={theme}>
      <StationsLimitedProvider>
        <RouterProvider router={router} />
      </StationsLimitedProvider>
    </ChakraProvider>
  </React.StrictMode>
);
