./gradlew jar
docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD
docker build --tag $DOCKER_USERNAME/spotify-wrapped-server:$CIRCLE_BRANCH -f Dockerfile- .
docker build --tag $DOCKER_USERNAME/spotify-wrapped-ui:$CIRCLE_BRANCH -f Dockerfile-ui .
docker push $DOCKER_USERNAME/spotify-wrapped-server:$CIRCLE_BRANCH
docker push $DOCKER_USERNAME/spotify-wrapped-ui:$CIRCLE_BRANCH
