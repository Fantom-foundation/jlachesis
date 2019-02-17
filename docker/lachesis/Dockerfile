# Alpine Linux with OpenJDK JRE
FROM openjdk:8-jre-alpine
RUN apk add --no-cache bash
WORKDIR /
EXPOSE 1337 1338 80
ADD ./jlachesis-0.2.1.jar jlachesis-0.2.1.jar
COPY ./lachesis/lachesis.sh lachesis.sh
COPY ./lachesis/keygen.sh keygen.sh
RUN chmod +x lachesis.sh keygen.sh
CMD ["/lachesis.sh"]