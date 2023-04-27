#!/bin/bash
ssh root@185.154.195.193 /bin/bash << EOF
rm -rf /root/bot-project
git clone -b dev git@github.com:ashNext/habr-telegram-bot.git /root/bot-project
sh /root/bot-project/sh/start-container.sh
EOF