export const metersToKilometers = (meters: number, decimals: number = 1) => {
  return (meters / 1000).toFixed(decimals);
};
