FROM java:8
VOLUME /tmp
ARG APP_PATH=/exchange
ARG ENVIRONMENT

RUN mkdir -p exchange-service
COPY ./target/exchange.jar ${APP_PATH}/exchange.jar
COPY ./target/config/${ENVIRONMENT}/application.yml ${APP_PATH}/application.yml
COPY ./target/config/${ENVIRONMENT}/templates/index.html ${APP_PATH}/templates/index.html
COPY ./target/config/${ENVIRONMENT}/templates/error.html ${APP_PATH}/templates/error.html

ARG CONFIG_FILE_PATH="-Dspring.config.location="${ENVIRONMENT}"/application.yml"

WORKDIR ${APP_PATH}

EXPOSE 8080
CMD java -jar exchange.jar $CONFIG_FILE_PATH