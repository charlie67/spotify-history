./gradlew jar
echo ${docker.DOCKER_PASSWORD} | docker login -u ${docker.DOCKER_USERNAME} --password-stdin
sudo docker build --tag ${docker.DOCKER_PASSWORD}/spotify-wrapped-server:${GITHUB_REF##*/} -f Dockerfile-server .
sudo docker build --tag ${docker.DOCKER_PASSWORD}/spotify-wrapped-ui:${GITHUB_REF##*/} -f Dockerfile-ui .
sudo docker push ${docker.DOCKER_PASSWORD}/spotify-wrapped-server:${GITHUB_REF##*/}
sudo docker push ${docker.DOCKER_PASSWORD}/spotify-wrapped-ui:${GITHUB_REF##*/}
