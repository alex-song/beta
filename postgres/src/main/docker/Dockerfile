FROM postgres:10.3
MAINTAINER Alex Song <song_liping@hotmail.com>
# Allow connections from anywhere.
COPY openConnections.sh /docker-entrypoint-initdb.d/
COPY pg_hba.conf $PGDATA/pg_hba_alex.conf
ENTRYPOINT ["docker-entrypoint.sh"]
EXPOSE 5432
CMD ["postgres"]
