./gradlew bootJar
echo $2 | docker login -u $1 --password-stdin
docker build --tag $1/spotify-history-server:${GITHUB_REF##*/} -f Dockerfile-server .
docker build --tag $1/spotify-history-ui:${GITHUB_REF##*/} -f Dockerfile-ui .
docker push $1/spotify-history-server:${GITHUB_REF##*/}
docker push $1/spotify-history-ui:${GITHUB_REF##*/}
