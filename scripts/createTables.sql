/*CREATE TABLE Ratings (
    userId NUMERIC(21,0),
    term VARCHAR(50),
    score DOUBLE,
    lastChanged TIMESTAMP,
    PRIMARY KEY (userId, term)
);*/

CREATE TABLE Views (
    userId NUMERIC(21,0),
    docNr INTEGER,
    itemId NUMERIC(21,0),
    PRIMARY KEY (userId, docNr, itemId)
);


