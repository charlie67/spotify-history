FROM eclipse-temurin:25.0.3_9-jre-alpine-3.23
COPY --from=caddy:2-alpine /usr/bin/caddy /usr/bin/caddy
COPY application/target/spotify-history-application-0.0.1-SNAPSHOT-exec.jar /app.jar
COPY ui/index.html /var/www/html/index.html
COPY ui/Caddyfile /etc/caddy/Caddyfile
EXPOSE 80
COPY entrypoint.sh entrypoint.sh
RUN chmod 777 entrypoint.sh
CMD ./entrypoint.sh
