/*

CREATE TABLE NewsSource (
	id INT NOT NULL AUTO_INCREMENT,
	description VARCHAR,
	fetchinterval INT,
	lastArticleFetchTime DATETIME,
	lastFetchTry DATETIME,
	`name` VARCHAR(255),
	rssUrl VARCHAR(255),
	PRIMARY KEY (id)
);

CREATE TABLE Ratings (
	userId NUMERIC(21,0),
	term VARCHAR(50),
	score DOUBLE,
	lastChanged TIMESTAMP,
	PRIMARY KEY (userId, term)
);

CREATE TABLE Views (
	userId NUMERIC(21,0),
	docNr INTEGER,
	itemId NUMERIC(21,0),
	PRIMARY KEY (userId, docNr, itemId)
);
*/

CREATE TABLE Trends (
	term VARCHAR(50),
	lastChanged TIMESTAMP,
	position INT,
	PRIMARY KEY (term)
);


