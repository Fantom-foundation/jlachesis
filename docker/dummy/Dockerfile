# Alpine Linux with OpenJDK JRE
FROM openjdk:8-jre-alpine
RUN apk add --no-cache bash
WORKDIR /
EXPOSE 8080
ADD ./jlachesis-0.2.1.jar jlachesis-0.2.1.jar
COPY ./dummy/dummy.sh dummy.sh
RUN chmod +x dummy.sh
CMD ["/dummy.sh"]