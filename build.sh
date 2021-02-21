./gradlew jar
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
docker build --tag $DOCKER_USERNAME/discord-music-bot:${CIRCLE_BRANCH}-server --push .
docker build --tag $DOCKER_USERNAME/discord-music-bot:${CIRCLE_BRANCH}-ui --push -f Dockerfile-ui .
