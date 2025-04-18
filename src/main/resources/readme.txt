

//create index
CREATE INDEX IF NOT EXISTS shipper_location_area_idx ON shipper_location (area);
CREATE INDEX IF NOT EXISTS shipper_location_shipper_id_idx ON shipper_location (shipper_id);