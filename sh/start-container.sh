#!/bin/bash
docker stop bot || true \
&& docker rm bot --force \
&& sh /root/habr-bot-envs.sh \
&& docker run -d -m 512m \
  --name bot \
  --network nw-pg-bot \
  -e DB_PASSWORD=$DB_PASSWORD \
  -e HABR_TG_BOT_TOKEN=$HABR_TG_BOT_TOKEN \
  -e REPORTING_TG_BOT_USER_ID=$REPORTING_TG_BOT_USER_ID \
  -v /root/pg-habr-bot:/root/work/habr-tg-bot \
  -v /root/work/gradle_caches:/root/.gradle \
  tg-bot

#  -v /root/work/gradle_caches:/root/.gradle \
#  -v "c:\Users\Next\Documents\gradle_caches:/root/.gradle" \

#  -v /root/pg-habr-bot:/root/work/habr-tg-bot \
#  -v "c:\Users\Next\Documents\Projects\Java\habr-telegram-bot:/root/work/pg-habr-bot" \