# Container / Image Cleanup
#sudo docker stop `sudo docker ps -a -q`
#sudo docker rm `sudo docker ps -a -q`
#sudo docker rmi `sudo docker images -q`

# Image Builds

sudo docker build -t server_image --file=Dockerfile_server .
sudo docker build -t client_image --file=Dockerfile_client .
sudo docker build -t dvc_image --file=Dockerfile_data_volume .

# Execute image on containers
sudo docker create -v /data --name dvc_container dvc_image
sudo docker run -itd --name=server_container --hostname=serverHost --volumes-from=dvc_container -P server_image
ip_add = $(sudo docker inspect --format '{{ .NetworkSettings.IPAddress }}' $(sudo docker ps -q))
sed s/serverHost/ip_add compilerscript_client.sh > compilerscript_client_new.sh
mv compilerscript_client_new.sh compilerscript_client.sh
sudo docker run -itd --name=client_container --volumes-from=dvc_container --link server_container -P client_image

# Testing
sudo docker ps -a
sleep 30
sudo docker ps -a
sudo docker logs server_container
sudo docker logs client_container

okay_cases=$(sudo docker logs client_container | grep 'OK' | wc -l )
echo $okay_cases
if [ $okay_cases -ge 10 ]
then
        echo "Passed"
else
        echo "Failed"
fi

