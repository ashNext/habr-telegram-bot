#!/bin/bash
docker stop bot || true \
&& docker rm bot --force \
&& docker rmi tg-bot --force \
&& docker build /root/bot-project -t tg-bot \
&& docker run -d -m 512m \
  --name bot \
  --network nw-pg-bot \
  -e DB_PASSWORD=$DB_PASSWORD \
  -e HABR_TG_BOT_TOKEN=$HABR_TG_BOT_TOKEN \
  -e REPORTING_TG_BOT_USER_ID=$REPORTING_TG_BOT_USER_ID \
  -v /root/gradle_caches:/root/.gradle \
  tg-bot

#  -v /root/work/gradle_caches:/root/.gradle \
#  -v "c:\Users\Next\Documents\gradle_caches:/root/.gradle" \