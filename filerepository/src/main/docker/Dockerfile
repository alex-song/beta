FROM develar/java:8u45
MAINTAINER Alex Song <song_liping@hotmail.com>
EXPOSE 7070
RUN mkdir /devlogs
VOLUME /devlogs
ADD filerepository-0.0.1.war filerepository.war
RUN sh -c 'touch /filerepository.war'
ENV JAVA_OPTS="-server -Duser.timezone=GMT+08"
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /filerepository.war" ]