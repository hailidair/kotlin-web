
-- 用户注册表   
create table ytdb.t_user_reg(
id int not null AUTO_INCREMENT,
user_name varchar(64),
password varchar(32),
guid varchar(64) not null,
primary key(id)
);

-- 用户登录表
create table ytdb.t_user_login(
id int not null auto_increment,
login_name varchar(64) not null,
password varchar(32),
nick_name varchar(32),
guid varchar(64) not null,
primary key(id),
UNIQUE(login_name)
);
-- 用户信息表
create table ytdb.t_user_info(
id int not null AUTO_INCREMENT,
user_name varchar(64),
phone varchar(32),
email varchar(32),
sex char(1),
guid varchar(64) not null,
primary key(id)
);
	
-- 用户会员表
create table ytdb.t_user_member(
id int not null auto_increment,
user_name varchar(64),
member_no varchar(10),
member_level varchar(32),
guid varchar(64) not null,
primary key(id)
)
