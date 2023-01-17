export const DirectionEnum = {
  Ascending: "asc",
  Descending: "desc"
} as const;
export type Direction = typeof DirectionEnum[keyof typeof DirectionEnum];
