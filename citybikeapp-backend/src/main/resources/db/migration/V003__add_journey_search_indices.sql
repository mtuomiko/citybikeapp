-- indices to support efficient keyset pagination on journey table
CREATE INDEX journey_arrival_at_idx ON journey (arrival_at);
CREATE INDEX journey_distance_idx ON journey (distance);
CREATE INDEX journey_duration_idx ON journey (duration);
