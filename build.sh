./gradlew jar
echo "Logging into docker"
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
echo "Logged into docker"
docker buildx build --platform linux/amd64 --tag $DOCKER_USERNAME/discord-music-bot:${CIRCLE_BRANCH}-server --push .
docker buildx build --platform linux/amd64 --tag $DOCKER_USERNAME/discord-music-bot:${CIRCLE_BRANCH}-ui --push -f Dockerfile-ui .
