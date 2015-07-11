FROM maven:3

ADD . /usr/src/app/
WORKDIR /usr/src/app

RUN mvn package jetty:effective-web-xml

EXPOSE 8443

CMD [ "mvn", "jetty:run" ]
