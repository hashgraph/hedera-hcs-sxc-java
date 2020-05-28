Simple docker image which setups a rabbit MQ server
-Should be configurable so that user/password and other necessary parameters can be setup without code changes

Run  `docker build --tag hh-rabbit:1.0 .` in this directory to build the image. 

In command line type

`docker run -d -e RABBITMQ_NODENAME=my-rabbit --name rabbitmq -p 9090:15672 -p 5672:5672 -p 15674:15674  hh-rabbit:1.0`


to download a rabbit image and run it with the management plugin installed, which allows web management. The image is based on the orginial rabbit-mq image but it also enables the stomp and the web-stomp plugin.

In a web-browser navigate to `localhost:9090` to access the management console.