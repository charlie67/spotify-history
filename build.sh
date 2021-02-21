./gradlew jar
docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD
docker build --tag $DOCKER_USERNAME/spotify-wrapped-server:$CIRCLE_BRANCH --push .
docker build --tag $DOCKER_USERNAME/spotify-wrapped-ui:$CIRCLE_BRANCH}--push -f Dockerfile-ui .
