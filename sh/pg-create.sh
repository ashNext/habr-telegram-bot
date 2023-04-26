#!/bin/bash
docker network create -d bridge nw-pg-bot || true \
&& docker stop pg-habr-bot || true \
&& docker rm pg-habr-bot --force \
&& docker pull postgres \
&& docker run -d -m 256m \
  --name pg-habr-bot \
  --network nw-pg-bot \
  --network-alias postgres \
  -v "/root/pg-data/pg-habr-bot:/var/lib/postgresql/data" \
  -e POSTGRES_PASSWORD=$DB_PASSWORD \
  -e POSTGRES_DB=habr_bot \
  -p 5432:5432 postgres