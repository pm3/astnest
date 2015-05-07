drop TABLE sb_user cascade;
drop TABLE sb_user_group  cascade;
drop TABLE sb_room  cascade;
drop TABLE sb_room_group  cascade;
drop TABLE sb_room_user  cascade;
drop TABLE sb_comment  cascade;
drop TABLE sb_like  cascade;
drop TABLE sb_att  cascade;
drop table sb_global_var cascade;
drop sequence batis_sequence;
drop sequence message_img_sequence;

create sequence batis_sequence;
create sequence message_img_sequence;

CREATE TABLE sb_user (
    id bigint DEFAULT nextval('batis_sequence') NOT NULL PRIMARY KEY,
    email varchar(200) NOT NULL UNIQUE,
    nick varchar(100) NOT NULL UNIQUE,
    phone varchar(50),
    org_unit varchar(100),
    work_position varchar(100),
    active bool NOT NULL,
    avatar_img varchar(200),
    background_img varchar(200),
    admin bool NOT NULL,
    last_login timestamp without time zone,
    password varchar(50), 
    unique(email), 
    unique(nick)
);

CREATE TABLE sb_user_group (
    user_id bigint NOT NULL references sb_user(id),
    name varchar(50) NOT NULL,
    unique(user_id, name)
);

CREATE TABLE sb_room (
    id bigint DEFAULT nextval('batis_sequence') NOT NULL PRIMARY KEY,
    name varchar(200) NOT NULL,
    path varchar(200) NOT NULL,
    description varchar(1000) NOT NULL,
    active bool NOT NULL,
    logo_img varchar(200),
    background_img varchar(200),
    auto_synchronized bool NOT NULL,
    modified timestamp without time zone NOT NULL,
    unique(path)
);

CREATE TABLE sb_room_group (
    room_id bigint NOT NULL references sb_room(id),
    name varchar(50) NOT NULL,
    group_access varchar(10),
    unique(room_id, name)
);

CREATE TABLE sb_room_user (
    room_id bigint NOT NULL references sb_room(id),
    user_id bigint NOT NULL references sb_user(id),
    with_group bool NOT NULL,
    access varchar(10),
    unique(room_id, user_id)
);

CREATE TABLE sb_message (
    id bigint DEFAULT nextval('batis_sequence') NOT NULL PRIMARY KEY,
    parent_id bigint references sb_comment(id),
    room_id bigint NOT NULL references sb_room(id),
    user_id bigint NOT NULL references sb_user(id),
    mtype varchar(15) NOT NULL,
    mtext varchar(2000) NOT NULL,
    images varchar(2000),
    likes int4 not null default 0,
    modified timestamp without time zone NOT NULL
);

CREATE TABLE sb_like (
    room_id bigint NOT NULL references sb_room(id),
    message_id bigint NOT NULL references sb_message(id),
    user_id bigint NOT NULL references sb_user(id),
    created timestamp NOT NULL,
    unique(comment_id, user_id)
);

create table sb_global_var (
    vkey varchar(50) NOT NULL PRIMARY KEY,
    val varchar(255)
);

insert into sb_user (email,nick,active,admin, password) values ('pm@aston.sk','admin',true,true, lower(md5('aston')));
