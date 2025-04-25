ALTER TABLE ted.ted_talk
    ADD COLUMN document tsvector;

update ted.ted_talk
set document = to_tsvector(title || ' ' || author);