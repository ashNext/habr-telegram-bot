FROM azul/zulu-openjdk:17-latest
ENV PROJ_HOME=/root/work/habr-tg-bot
ENV SPRING_PROFILES_ACTIVE=prod
ENV DB_PASSWORD=$DB_PASSWORD
ENV HABR_TG_BOT_TOKEN=$HABR_TG_BOT_TOKEN
ENV REPORTING_TG_BOT_USER_ID=$REPORTING_TG_BOT_USER_ID
#COPY . $PROJ_HOME
WORKDIR $PROJ_HOME
ADD build-and-run.sh /root/work/build-and-run.sh
RUN chmod +x /root/work/build-and-run.sh
ENTRYPOINT ["/root/work/build-and-run.sh"]