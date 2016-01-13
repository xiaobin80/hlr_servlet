CREATE TABLE trainOrder (
	train_number int NULL ,
	seriary_number int NULL ,
	car_number varchar (30) ,
	car_marque varchar (30) ,
	carry_weight1 numeric(9, 3) NULL ,
	self_weight1 numeric(9, 3) NULL ,
	past_time nvarchar (50) ,
	outFlag nvarchar (50) ,
	badFlag bit DEFAULT 1 NOT NULL ,
	year_level2 varchar (30) ,
	month_level3 varchar (30) ,
	sn int NOT NULL AUTO_INCREMENT ,
        PRIMARY KEY (sn))AUTO_INCREMENT=100001;