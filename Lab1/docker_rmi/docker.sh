# Container / Image Cleanup
docker stop pingpongclient pingpongserver
docker rm pingpongclient pingpongserver
docker rmi pingpongclient pingpongserver



# Dockerfile for PingPongServer
cat > pingpongserver << EOF2
FROM jamespedwards42/docker-ubuntu-java-8:latest
ADD ServerInterface.java ./ServerInterface.java
ADD RMIException.java ./RMIException.java
ADD Skeleton.java ./Skeleton.java
ADD Stub.java ./Stub.java
ADD PingPongServer.java ./PingPongServer.java
ADD PingPongClient.java ./PingPongClient.java
ADD PingServerFactory.java ./PingServerFactory.java
RUN javac ServerInterface.java RMIException.java Skeleton.java Stub.java PingPongServer.java PingPongClient.java PingServerFactory.java 
CMD java PingPongServer
EOF2



# Dockerfile for PingPongClient
cat > pingpongclient << EOF3
FROM jamespedwards42/docker-ubuntu-java-8:latest
ADD ServerInterface.java ./ServerInterface.java
ADD RMIException.java ./RMIException.java
ADD Skeleton.java ./Skeleton.java
ADD Stub.java ./Stub.java
ADD PingPongServer.java ./PingPongServer.java
ADD PingPongClient.java ./PingPongClient.java
ADD PingServerFactory.java ./PingServerFactory.java
RUN javac ServerInterface.java RMIException.java Skeleton.java Stub.java PingPongServer.java PingPongClient.java 
CMD java PingPongClient
EOF3


# Image Builds
docker build -t pingpongserver --file=pingpongserver .
docker build -t pingpongclient --file=pingpongclient .


# Execute image on containers
docker run -itd --name=pingpongserver  --hostname=serverHost -P pingpongserver
docker run -itd --name=pingpongclient --link pingpongserver -P pingpongclient


# Testing
docker ps -a
docker logs pingpongserver
docker logs -f pingpongclient
docker stop pingpongclient

okay_cases=$(docker logs pingpongclient | grep 'Test cases failed: 0' | wc -l )
if [ $okay_cases -ge 4 ]
then
echo "Passed"
else
echo "Failed"
fi
