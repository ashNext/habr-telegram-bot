#!/bin/bash
docker stop bot || true \
&& docker rm bot --force \
&& docker rmi tg-bot --force \
&& docker build /root/bot-project/sh -t tg-bot \
