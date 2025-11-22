-- Allow NULL values in vehicle.updated_at
ALTER TABLE vehicle
    ALTER COLUMN updated_at DROP NOT NULL;