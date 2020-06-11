ALTER TABLE gateway_cache ADD uprn varchar (255);
ALTER TABLE gateway_cache ADD estab_uprn varchar (255);
CREATE INDEX idx_uprn ON gateway_cache (uprn);