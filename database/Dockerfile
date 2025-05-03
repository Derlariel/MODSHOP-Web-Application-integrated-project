FROM mysql
COPY ./mobile_shop.sql /docker-entrypoint-initdb.d/init.sql
RUN chmod 755 /docker-entrypoint-initdb.d/init.sql