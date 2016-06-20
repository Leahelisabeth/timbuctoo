FROM maven:3.3-jdk-8

COPY . /root/timbuctoo-build

WORKDIR /root/timbuctoo-build

RUN ls

RUN mvn clean package

WORKDIR /root/timbuctoo-build/timbuctoo-instancev4/docker

EXPOSE 80
EXPOSE 8081
ENTRYPOINT ["./run.sh", "-y", "./config.yaml"]
