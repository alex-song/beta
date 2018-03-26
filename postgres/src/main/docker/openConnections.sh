#!/usr/bin/env bash
set -e

{
	echo
	echo "host all all 0.0.0.0/0 md5"
	echo "host all all 127.0.0.1/32 md5"
} >> "$PGDATA/pg_hba.conf"

{
    echo
    echo "listen_addresses = '*'"
} >> "$PGDATA/postgresql.conf"