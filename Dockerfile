FROM java:8
VOLUME /tmp
ARG APP_PATH=/exchange
ARG ENVIRONMENT

RUN mkdir -p exchange-service
COPY ./target/exchange.jar ${APP_PATH}/exchange.jar
COPY ./target/config/dev/application.yml ${APP_PATH}/application.yml

ARG CONFIG_FILE_PATH="-Dspring.config.location="${ENVIRONMENT}"/application.yml"

WORKDIR ${APP_PATH}

EXPOSE 8080
CMD java -jar exchange.jar $CONFIG_FILE_PATH