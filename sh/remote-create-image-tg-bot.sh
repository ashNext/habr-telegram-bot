#!/bin/bash
ssh root@185.154.195.193 /bin/bash << EOF
rm -rf /root/habr-tg-bot
git clone -b dev git@github.com:ashNext/habr-telegram-bot.git /root/habr-tg-bot
sh /root/habr-tg-bot/sh/tg-bot-create.sh
EOF