CREATE TABLE `User` (ID int(10) NOT NULL AUTO_INCREMENT, Email varchar(255), Username varchar(255), Password varchar(255), Role int(10),  ProfileImage varchar(255), PremiumEndDate datetime, PRIMARY KEY (ID));
CREATE TABLE PontoInteresse (ID int(10) NOT NULL AUTO_INCREMENT, Latitude double,Longitude double, Name varchar(255), Description varchar(1000), Dificulty int(10), Accessibility bit(1), State bit(1),Premium bit(1), Score double, CreationDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (ID),CreatorEmail varchar(255));
CREATE TABLE Review (ID int(10) NOT NULL AUTO_INCREMENT, Comment varchar(1000), Rating int(10), CreationDate DATETIME, UserID int(10) NOT NULL, PontoInteresseID int(10) NOT NULL, PRIMARY KEY (ID));
CREATE TABLE Image (ID int(10) NOT NULL AUTO_INCREMENT, Url varchar(255), ReviewID int(10) NOT NULL, PRIMARY KEY (ID));
CREATE TABLE PontoInteresse_User (PontoInteresseID int(10) NOT NULL, UserID int(10) NOT NULL, PRIMARY KEY (PontoInteresseID, UserID));
CREATE TABLE ImagePontoInteresse (ID int(10) NOT NULL AUTO_INCREMENT,Url varchar(255) NOT NULL,PontoInteresseID int(10) NOT NULL,PRIMARY KEY (ID));


ALTER TABLE Review ADD CONSTRAINT FKReview970746 FOREIGN KEY (UserID) REFERENCES `User` (ID);
ALTER TABLE Image ADD CONSTRAINT FKImage267607 FOREIGN KEY (ReviewID) REFERENCES Review (ID);
ALTER TABLE Review ADD CONSTRAINT FKReview616691 FOREIGN KEY (PontoInteresseID) REFERENCES PontoInteresse (ID);
ALTER TABLE PontoInteresse_User ADD CONSTRAINT FKPontoInter992724 FOREIGN KEY (PontoInteresseID) REFERENCES PontoInteresse (ID);
ALTER TABLE PontoInteresse_User ADD CONSTRAINT FKPontoInter594713 FOREIGN KEY (UserID) REFERENCES `User` (ID);
ALTER TABLE ImagePontoInteresse ADD CONSTRAINT FK_ImagePontoInteresse_PontoInteresse FOREIGN KEY (PontoInteresseID) REFERENCES PontoInteresse(ID);


-- Insert Users
INSERT INTO `User` (Email, Username, Password, Role,ProfileImage, PremiumEndDate) VALUES
('alice@example.com', 'alice', 'hashedpassword1', 1,"example.com",'2025-12-31'),
('bob@example.com', 'bob', 'hashedpassword2', 2,"example.com",NULL),
('charlie@example.com', 'charlie', 'hashedpassword3', 3,"example.com",'2025-06-30');

-- Insert Points of Interest
INSERT INTO PontoInteresse (Latitude,Longitude, Name, Description, Dificulty, Accessibility, State,Premium, Score) VALUES
(40.7128,-74.0060, 'Central Park', 'A large public park in NYC.', 2, b'1', b'1',b'1' ,4.5,"alice@example.com"),
(48.8584,2.2945, 'Eiffel Tower', 'Famous landmark in Paris.', 3, b'1', b'1', b'0',4.8,2,"alice@example.com"),
(35.6895,139.6917, 'Mount Takao', 'Popular hiking trail near Tokyo.', 4, b'0', b'1',b'0', 4.2,"alice@example.com");

-- Insert Reviews
INSERT INTO Review (Comment, Rating, CreationDate, UserID, PontoInteresseID) VALUES
('Beautiful and relaxing place!', 5, '2025-04-01', 1, 1),
('Great view, but very crowded.', 4, '2025-04-03', 2, 2),
('Challenging hike, but worth it!', 5, '2025-04-05', 3, 3);

-- Insert Images
INSERT INTO Image (Url, UserID, ReviewID) VALUES
('https://example.com/img1.jpg', 1, 1),
('https://example.com/img2.jpg', 2, 2),
('https://example.com/img3.jpg', 3, 3);

-- Insert PontoInteresse_User (Saved Points)
INSERT INTO PontoInteresse_User (PontoInteresseID, UserID) VALUES
(1, 1),
(2, 1),
(3, 2),
(1, 3);

-- Insert Images for PontoInteresse
INSERT INTO ImagePontoInteresse (Url, PontoInteresseID) VALUES
('https://example.com/poi1_img1.jpg', 1),
('https://example.com/poi2_img1.jpg', 2),  
('https://example.com/poi3_img1.jpg', 3),  
('https://example.com/poi1_img2.jpg', 1);  

