FROM maven:3.9.8-eclipse-temurin-21 AS builder
WORKDIR /workspace

COPY . /workspace
RUN mvn -f /workspace/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jdk-jammy
RUN apt-get update \
    && apt-get install -y nginx openssh-server \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /var/run/sshd /app /etc/nginx/conf.d
COPY --from=builder /workspace/target/*.jar /app/idcard-manager.jar
COPY nginx/default.conf /etc/nginx/conf.d/default.conf
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

EXPOSE 8080 22
ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
