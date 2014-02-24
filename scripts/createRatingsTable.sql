CREATE TABLE Ratings (
    userId NUMERIC(21,0),
    term VARCHAR(50),
    score DOUBLE,
    lastChanged TIMESTAMP,
    PRIMARY KEY (userId, term)
);
