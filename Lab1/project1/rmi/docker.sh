# Container / Image Cleanup
docker stop pingpongclient pingpongserver
docker rm pingpongclient pingpongserver
docker rmi pingpongclient pingpongserver



# Dockerfile for PingPongServer
cat > pingpongserver << EOF2
FROM java
ADD Hello.java ./Hello.java
ADD RMIException.java ./RMIException.java
ADD Skeleton.java ./Skeleton.java
ADD Server.java ./Server.java
RUN javac Hello.java RMIException.java Skeleton.java Server.java
CMD java Server
EOF2



# Dockerfile for PingPongClient
cat > pingpongclient << EOF3
FROM java
ADD Hello.java ./Hello.java
ADD RMIException.java ./RMIException.java
ADD Skeleton.java ./Skeleton.java
ADD Stub.java ./Stub.java
ADD Client.java ./Client.java
RUN javac Hello.java RMIException.java Skeleton.java Stub.java Client.java
CMD java Client
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

okay_cases=$(docker logs client | grep 'Test cases failed: 0' | wc -l )
if [ $okay_cases -ge 4 ]
then
echo "Passed"
else
echo "Failed"
fi
