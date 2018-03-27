#!/usr/bin/env bash
set -e

#{
#	echo
#	echo "host    all    all        0.0.0.0/0     md5"
#	echo "host    all    all        0.0.0.0/0     password"
#	echo "host    all    all        ::0/0         md5"
#	echo "host    all    all        ::0/0         password"
#} >> "$PGDATA/pg_hba.conf"

mv -f $PGDATA/pg_hba.conf $PGDATA/pg_hba_org.conf
cp pg_hba.conf $PGDATA/pg_hba.conf