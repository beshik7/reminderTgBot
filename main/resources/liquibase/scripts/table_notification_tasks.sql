--liquibase formatted sql

--changeset beshik7:1
create table notification_task
(
    id serial PRIMARY KEY,
    chat_id BIGINT NOT NULL,
    notification TEXT NOT NULL,
    reminder_date TIMESTAMP NOT NULL,
    time_added TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);