./gradlew jar
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
docker build --tag $DOCKER_USERNAME:spotify-wrapped-server:${CIRCLE_BRANCH} --push .
docker build --tag $DOCKER_USERNAME:spotify-wrapped-ui:${CIRCLE_BRANCH} --push -f Dockerfile-ui .
