./gradlew jar
echo ${DOCKER_PASSWORD} | docker login -u ${DOCKER_USERNAME} --password-stdin
sudo docker build --tag ${DOCKER_PASSWORD}/spotify-wrapped-server:${GITHUB_REF##*/} -f Dockerfile-server .
sudo docker build --tag ${DOCKER_PASSWORD}/spotify-wrapped-ui:${GITHUB_REF##*/} -f Dockerfile-ui .
sudo docker push ${DOCKER_PASSWORD}/spotify-wrapped-server:${GITHUB_REF##*/}
sudo docker push ${DOCKER_PASSWORD}/spotify-wrapped-ui:${GITHUB_REF##*/}
