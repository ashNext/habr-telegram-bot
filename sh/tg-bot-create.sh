#!/bin/bash
docker stop bot || true \
&& docker rm bot --force \
&& docker rmi tg-bot --force \
&& docker build /root/habr-tg-bot/sh -t tg-bot \
