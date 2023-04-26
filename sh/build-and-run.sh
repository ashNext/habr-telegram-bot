#!/bin/bash

echo PROJ_HOME=$PROJ_HOME
echo SPRING_PROFILES_ACTIVE=$SPRING_PROFILES_ACTIVE
echo DB_PASSWORD=$DB_PASSWORD
echo HABR_TG_BOT_TOKEN=$HABR_TG_BOT_TOKEN
echo REPORTING_TG_BOT_USER_ID=$REPORTING_TG_BOT_USER_ID
cd $PROJ_HOME

echo '--START--'

echo '--BUILDING--'
bash ./gradlew bootJar
echo '--BUILD-END--'

echo '--RUNNING--'
ls $PROJ_HOME/build/libs
java -jar $PROJ_HOME/build/libs/habr-telegram-bot-0.0.1-SNAPSHOT.jar --spring.profiles.active=$SPRING_PROFILES_ACTIVE
echo '--STOP--'

echo '--DONE--'