CREATE TABLE IF NOT EXISTS students (
    id    SERIAL PRIMARY KEY,
    name  VARCHAR(100) NOT NULL,
    phone VARCHAR(20)  NOT NULL UNIQUE,
    grade INT          NOT NULL CHECK (grade BETWEEN 1 AND 11)
);

CREATE TABLE IF NOT EXISTS tutors (
    id              SERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    phone           VARCHAR(20)  NOT NULL UNIQUE,
    hourly_rate_som INT          NOT NULL CHECK (hourly_rate_som > 0)
);

CREATE TABLE IF NOT EXISTS subjects (
    id   SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS lessons (
    id         SERIAL PRIMARY KEY,
    student_id INT          NOT NULL REFERENCES students (id),
    tutor_id   INT          NOT NULL REFERENCES tutors (id),
    subject_id INT          NOT NULL REFERENCES subjects (id),
    starts_at  TIMESTAMP    NOT NULL,
    -- Java has OnlineLesson / OfflineLesson; a table has no inheritance, so the type is a column
    type       VARCHAR(10)  NOT NULL CHECK (type IN ('ONLINE', 'OFFLINE')),
    -- meeting link for ONLINE, address for OFFLINE
    location   VARCHAR(200) NOT NULL,
    status     VARCHAR(10)  NOT NULL CHECK (status IN ('BOOKED', 'DONE', 'CANCELLED')),
    -- price calculated by OnlineLesson / OfflineLesson at booking time; the report sums this column
    price_som  INT          NOT NULL CHECK (price_som >= 0)
);
