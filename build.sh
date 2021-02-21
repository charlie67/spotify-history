./gradlew bootJar
echo $2 | docker login -u $1 --password-stdin
sudo docker build --tag $1/spotify-history-server:${GITHUB_REF##*/} -f Dockerfile-server .
sudo docker build --tag $1/spotify-history-ui:${GITHUB_REF##*/} -f Dockerfile-ui .
sudo docker push $1/spotify-history-server:${GITHUB_REF##*/}
sudo docker push $1/spotify-history-ui:${GITHUB_REF##*/}
