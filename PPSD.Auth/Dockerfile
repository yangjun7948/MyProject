FROM anapsix/alpine-java:8_server-jre_unlimited

MAINTAINER 梓云科技

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

RUN mkdir -p /zykj-auth

WORKDIR /zykj-auth

EXPOSE 3000

ADD ./target/zykj-auth.jar ./

CMD sleep 30;java -Djava.security.egd=file:/dev/./urandom -jar zykj-auth.jar
