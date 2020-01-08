# hcs-queue

A component of an hcs network which queues messages for delivery to apps

Example ActiveMQ Artemis docker image

`docker pull vromero/activemq-artemis`

https://github.com/vromero/activemq-artemis-docker/blob/master/README.md

run with 

`docker run -it --rm -p 8161:8161 -p 61616:61616 -e DISABLE_SECURITY=true  vromero/activemq-artemis`