./gradlew bootJar
echo $2 | docker login -u $1 --password-stdin
docker build --tag $1/spotify-history:${GITHUB_REF##*/} -f Dockerfile-server .
docker push $1/spotify-history:${GITHUB_REF##*/}
