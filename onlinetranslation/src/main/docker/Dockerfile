FROM frolvlad/alpine-oraclejdk8:slim
MAINTAINER Alex Song <song_liping@hotmail.com>
EXPOSE 7070
VOLUME /tmp
ADD onlinetranslation-0.0.1.war app.war
RUN sh -c 'mkdir -p /logs'
RUN sh -c 'touch /app.war'
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.war" ]