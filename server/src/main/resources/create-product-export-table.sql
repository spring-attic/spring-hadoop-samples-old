drop table if exists product_export;

create table product_export (
	id varchar(255) not null,
	name varchar(255) not null,
	description varchar(255),
	price float not null,
	primary key (id)
);
