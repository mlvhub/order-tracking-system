create schema order_tracker;

create table users (
    id UUID primary key,
    email text not null unique,
    name text not null,
    password text not null
);
