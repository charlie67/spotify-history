./gradlew jar
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
sudo docker build --tag $DOCKER_USERNAME/spotify-wrapped-server:$CIRCLE_BRANCH -f Dockerfile-server .
sudo docker build --tag $DOCKER_USERNAME/spotify-wrapped-ui:$CIRCLE_BRANCH -f Dockerfile-ui .
sudo docker push $DOCKER_USERNAME/spotify-wrapped-server:$CIRCLE_BRANCH
sudo docker push $DOCKER_USERNAME/spotify-wrapped-ui:$CIRCLE_BRANCH
