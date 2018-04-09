drop table grouptable;
drop table studentcourse;
drop table usertable;
drop table coursetable;
drop table filestorage;
drop table appointments;

create table FILESTORAGE (
    FILE_ID INT NOT NULL AUTO_INCREMENT,
    FILE_NAME VARCHAR(255),
    FILE_TYPE VARCHAR(255),
    FILE_SIZE BIGINT,
    FILE_CONTENTS MEDIUMBLOB ,  /* binary data */
    USERNAME VARCHAR(255),
    PRIMARY KEY (FILE_ID)
);

create table APPOINTMENTS (
    ID INT NOT NULL AUTO_INCREMENT,
    ADVISOR varchar(255),
    ADVISOR_LAST varchar(255),
    APP_DATE date,
    APP_TIME time,
    STUDENT varchar(255) DEFAULT NULL,
    PRIMARY KEY (ID)
);

create table USERTABLE (
    ID INT NOT NULL AUTO_INCREMENT,
    CODE varchar(12),
    /*ACTIVATED BOOLEAN,*/
    USERNAME varchar(255),
    FIRSTNAME varchar(255),
    LASTNAME varchar(255),
    PASSWORD char(64), /* SHA-256 encryption */
    UCO_ID INT,
    MAJOR varchar(4),
    PHONE varchar(12),
    ADVISED bit DEFAULT 0,
    unique (UCO_ID),
    primary key (id)
);

create table GROUPTABLE (
    ID INT NOT NULL AUTO_INCREMENT,
    GROUPNAME varchar(255),
    USERNAME varchar(255),
    primary key (id)
);

create table COURSETABLE (
    COURSENAME varchar(255),
    COURSEPREFIX varchar(255),
    COURSENUMBER varchar(255),
    CRN varchar(255) NOT NULL,
    PREREQUISITE varchar(255),
    primary key (CRN)
);

create table STUDENTCOURSE(
    STUDENTID INT,
    CRN varchar(255),
    COMPLETED bit DEFAULT 0,
    foreign key(STUDENTID) references USERTABLE(ID),
    foreign key(CRN) references COURSETABLE(CRN)
);


/*
    *** INITIAL ENTRIES HERE ***
    root (password='ppp'): advisorgroup,studentgroup
    christian.magueyal@gmail.com (password='ppp'): advisorgroup
    wmcdaniel@uco.edu (password='ppp'): advisorgroup
    jsmith@uco.edu (password='ppp'): studentgroup
    cmagueyal@uco.edu (password='ppp'): studentgroup
*/
insert into USERTABLE (username, firstname, lastname, password, phone)
    values ('root', 'root', 'root',
        'c4289629b08bc4d61411aaa6d6d4a0c3c5f8c1e848e282976e29b6bed5aeedc7',
        '555-321-3214');
insert into GROUPTABLE (groupname, username) values ('advisorgroup', 'root');

insert into USERTABLE (username, firstname, lastname, password, phone)
    values ('christian.magueyal@gmail.com', 'Christian', 'Sanchez',
        'c4289629b08bc4d61411aaa6d6d4a0c3c5f8c1e848e282976e29b6bed5aeedc7',
        '555-123-1234');
insert into GROUPTABLE (groupname, username) values ('advisorgroup', 'christian.magueyal@gmail.com');
insert into USERTABLE (username, firstname, lastname, password, phone)
    values ('wmcdaniel@uco.edu', 'William', 'McDaniel',
        'c4289629b08bc4d61411aaa6d6d4a0c3c5f8c1e848e282976e29b6bed5aeedc7',
        '555-123-2244');
insert into GROUPTABLE (groupname, username) values ('advisorgroup', 'wmcdaniel@uco.edu');

insert into USERTABLE (username, firstname, lastname, password, phone, uco_id, major)
    values ('jsmith@uco.edu', 'John', 'Smith',
        'c4289629b08bc4d61411aaa6d6d4a0c3c5f8c1e848e282976e29b6bed5aeedc7',
        '555-123-1234', 11223344, '6100');
insert into GROUPTABLE (groupname, username) values ('studentgroup', 'jsmith@uco.edu');

insert into USERTABLE (username, firstname, lastname, password, phone, uco_id, major)
    values ('cmagueyal@uco.edu', 'Christian', 'Magueyal',
        'c4289629b08bc4d61411aaa6d6d4a0c3c5f8c1e848e282976e29b6bed5aeedc7',
        '555-123-1234', 12341234, '6101');
insert into GROUPTABLE (groupname, username) values ('studentgroup', 'cmagueyal@uco.edu');

insert into COURSETABLE (coursename, courseprefix, coursenumber,crn, prerequisite)
    values ('Programming I', 'CMSC', '1613', '21047', null);
insert into COURSETABLE (coursename, courseprefix, coursenumber,crn, prerequisite)
    values ('Programming I Lab', 'CMSC', '1621', '21562', null);
insert into COURSETABLE (coursename, courseprefix, coursenumber,crn, prerequisite)
    values ('Beginning Programming', 'CMSC', '1513', '21172', null);
insert into COURSETABLE (coursename, courseprefix, coursenumber,crn, prerequisite)
    values ('Programming II', 'CMSC', '2613', '20975', '21047');
insert into COURSETABLE (coursename, courseprefix, coursenumber,crn, prerequisite)
    values ('Intro to Computing Systems', 'CMSC', '1103', '21847', null);

insert into STUDENTCOURSE (studentid, crn,completed)
    values (4, '21047',1);
insert into STUDENTCOURSE (studentid, crn, completed)
    values (5, '21172',1);
insert into STUDENTCOURSE (studentid, crn, completed)
    values (4, '21562',1);
insert into STUDENTCOURSE (studentid, crn, completed)
    values (5, '21847',1);
insert into STUDENTCOURSE (studentid, crn, completed)
    values (4, '20975',1);
insert into STUDENTCOURSE (studentid, crn)
    values (4, '21847');
insert into STUDENTCOURSE (studentid, crn)
    values (5, '21047');
insert into STUDENTCOURSE (studentid, crn)
    values (5, '21562');

insert into APPOINTMENTS (advisor, advisor_last, app_date, app_time, student)
    values ('christian.magueyal@gmail.com', 'Sanchez', '2017-04-05', '13:40:00', 'jsmith@uco.edu');
insert into APPOINTMENTS (advisor, advisor_last, app_date, app_time, student)
    values ('christian.magueyal@gmail.com', 'Sanchez', '2017-04-04', '14:15:00', 'cmagueyal@uco.edu');
insert into APPOINTMENTS (advisor, advisor_last, app_date, app_time, student)
    values ('wmcdaniel@uco.edu', 'McDaniel', '2017-04-15', '13:40:00', 'jsmith@uco.edu');
insert into APPOINTMENTS (advisor, advisor_last, app_date, app_time, student)
    values ('wmcdaniel@uco.edu', 'McDaniel', '2017-04-14', '8:35:00', 'cmagueyal@uco.edu');
insert into APPOINTMENTS (advisor, advisor_last, app_date, app_time, student)
    values ('christian.magueyal@gmail.com', 'Sanchez', '2017-05-05', '13:40:00', null);
insert into APPOINTMENTS (advisor, advisor_last, app_date, app_time, student)
    values ('christian.magueyal@gmail.com', 'Sanchez', '2017-04-04', '15:15:00', null);
insert into APPOINTMENTS (advisor, advisor_last, app_date, app_time, student)
    values ('wmcdaniel@uco.edu', 'McDaniel', '2017-04-15', '14:40:00', null);
insert into APPOINTMENTS (advisor, advisor_last, app_date, app_time, student)
    values ('wmcdaniel@uco.edu', 'McDaniel', '2017-04-14', '9:35:00', null);

insert into filestorage(FILE_NAME,FILE_TYPE, FILE_SIZE, FILE_CONTENTS, USERNAME)
    values(null,null,null, null ,'jsmith@uco.edu');
insert into filestorage(FILE_NAME,FILE_TYPE, FILE_SIZE, FILE_CONTENTS, USERNAME)
    values(null,null,null, null ,'cmagueyal@uco.edu');