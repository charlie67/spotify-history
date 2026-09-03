-- Refresh tokens issued by Spotify expire six months after they are issued (announced 18 June
-- 2026, in force from 20 July 2026). Recording when a token was stored lets the application say how
-- long is left before the user has to sign in again, rather than only finding out when a refresh is
-- rejected. Existing rows are left null: the issue date of a token stored before this migration is
-- not known, and guessing at it would report an expiry date that is quietly wrong.
ALTER TABLE token
    ADD COLUMN issued_at TIMESTAMP WITH TIME ZONE;
