-- Indexing only departure timestamp since it will be the deciding timestamp
-- BRIN index an interesting option? Would need to handle at least insertion order of journeys (also late inserts could
-- mess things up).
CREATE INDEX journey_departure_at_idx ON journey (departure_at);
