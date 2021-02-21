./gradlew jar
echo ${secrets.DOCKER_PASSWORD} | docker login -u ${secrets.DOCKER_USERNAME} --password-stdin
sudo docker build --tag ${secrets.DOCKER_USERNAME}/spotify-wrapped-server:${GITHUB_REF##*/} -f Dockerfile-server .
sudo docker build --tag ${secrets.DOCKER_USERNAME}/spotify-wrapped-ui:${GITHUB_REF##*/} -f Dockerfile-ui .
sudo docker push ${secrets.DOCKER_USERNAME}/spotify-wrapped-server:${GITHUB_REF##*/}
sudo docker push ${secrets.DOCKER_USERNAME}/spotify-wrapped-ui:${GITHUB_REF##*/}
