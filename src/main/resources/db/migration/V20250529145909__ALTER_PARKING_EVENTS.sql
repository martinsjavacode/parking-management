ALTER TABLE parking_events
    ADD COLUMN price_multiplier NUMERIC(3, 2);

ALTER TABLE parking_events
    ADD COLUMN amount_paid NUMERIC(10, 2);
