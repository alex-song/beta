FROM develar/java:8u45
MAINTAINER Alex Song <song_liping@hotmail.com>
EXPOSE 7070
RUN mkdir /devlogs
VOLUME /devlogs
ADD filerepository-0.0.1.war filerepository.war
RUN sh -c 'touch /filerepository.war'
ENV JAVA_OPTS="-server -Duser.timezone=GMT+08"
ENV DATABASE_OPTS=""
ENV FRS_CONFIG=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS $DATABASE_OPTS $FRS_CONFIG -Djava.security.egd=file:/dev/./urandom -jar /filerepository.war" ]