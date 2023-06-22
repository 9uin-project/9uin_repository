create table user(
    id        bigint         not null generated by default as identity  primary key,
    user_id   varchar(255)   not null,
    password  varchar(255)   not null,
    mail      varchar(255)   not null,
    createAt  datetime,
    updateAt  datetime
)

create table post(
    id        bigint         not null generated by default as identity  primary key,
    type      varchar(255)   not null,
    title     varchar(255)   not null,
    text      varchar(255)   not null,
    comment_cnt int ,
    user_id   bigint         not null,
    foreign key(user_id) references user(id) on delete cascade,
    createAt  datetime,
    updateAt  datetime
)

create table comment(
    id        bigint         not null generated by default as identity  primary key,
    text      varchar(255)   not null,
    user_id   bigint         not null,
    post_id   bigint         not null,
    foreign key(user_id) references user(id) on delete cascade,
    foreign key(post_id) references post(id) on delete cascade,
    createAt  datetime,
    updateAt  datetime
)

create table skilltag(
    id       bigint          not null generated by default as identity  primary key,
    name     varchar(255)    not null
)

create table tagPostRelation(
    post_id      bigint          not null,
    skilltag_id  bigint          not null,
    foreign key(post_id) references post(id) on delete cascade,
    foreign key(skilltag_id) references skilltag(id) on delete cascade
)