ALTER TABLE library ADD COLUMN binned_added_date timestamp;
UPDATE library SET binned_added_date = date_bin('12 hours', added_date, '2020-01-01');
CREATE INDEX idx_library_binned_added_date ON library(binned_added_date);

CREATE FUNCTION update_binned_added_date() RETURNS trigger AS $$
BEGIN
  NEW.binned_added_date := date_bin('12 hours', NEW.added_date, '2020-01-01');
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_binned_added_date
BEFORE INSERT OR UPDATE ON library
FOR EACH ROW
EXECUTE FUNCTION update_binned_added_date();

DROP INDEX IF EXISTS idx_library_binned_added_date;
CREATE INDEX idx_library_binned_added_date_covering
  ON library (binned_added_date)
  INCLUDE (buy_price);