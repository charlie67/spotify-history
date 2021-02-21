echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
docker buildx build --platform linux/amd64 --tag $DOCKER_USERNAME/discord-music-bot:${BRANCH_NAME}-server --push .
docker buildx build --platform linux/amd64 --tag $DOCKER_USERNAME/discord-music-bot:${BRANCH_NAME}-ui --push -f Dockerfile-ui .