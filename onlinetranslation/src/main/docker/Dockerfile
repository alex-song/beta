FROM frolvlad/alpine-oraclejdk8:slim
MAINTAINER Alex Song <song_liping@hotmail.com>
EXPOSE 7070
VOLUME /devlogs
ADD onlinetranslation-0.0.1-springboot.war onlinetranslation.war
RUN sh -c 'mkdir -p /devlogs'
RUN sh -c 'touch /onlinetranslation.war'
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /onlinetranslation.war" ]