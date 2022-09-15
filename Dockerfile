FROM openjdk:17-buster
RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y nginx
COPY build/libs/spotify-play-history-0.0.1.jar /app.jar
COPY ui/index.html /var/www/html/index.html
COPY ui/nginx.conf /etc/nginx/sites-enabled/nginx.conf
EXPOSE 80
COPY entrypoint.sh entrypoint.sh
RUN chmod 777 entrypoint.sh
RUN echo "daemon off;" >> /etc/nginx/nginx.conf
RUN rm /etc/nginx/sites-enabled/default
CMD ./entrypoint.sh