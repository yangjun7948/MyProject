FROM anapsix/alpine-java:8_server-jre_unlimited

MAINTAINER 梓云科技

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

RUN mkdir -p /zykj-file

WORKDIR /zykj-file

EXPOSE 7000

ADD ./target/zykj-file.jar ./

CMD sleep 30;java -Djava.security.egd=file:/dev/./urandom -jar zykj-file.jar
