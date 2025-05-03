FROM mysql

COPY ./mobile_shop.sql /docker-entrypoint-initdb.d/init.sql

RUN chmod 755 /docker-entrypoint-initdb.d/init.sql

ENV MYSQL_ROOT_PASSWORD=root_password
ENV MYSQL_DATABASE=your_database_name
ENV MYSQL_CHARSET=utf8mb4
ENV MYSQL_TIMEZONE=Asia/Bangkok

COPY ./my.cnf /etc/mysql/conf.d/my.cnf