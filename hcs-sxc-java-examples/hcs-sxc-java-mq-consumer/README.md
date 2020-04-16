# hcs-sxc-java-mq-consumer

Consumes messages from an MQ message queue (Rabbit MQ) and pushes the messages to HCS.
A generator is included in this project for testing purposes.

## Build

run `./mvnw clean install`, this will compile all the elements of the project and generate docker images for the listener and generator.

- hederahashgraph/hcs-sxc-java-queue-listener:latest
- hederahashgraph/hcs-sxc-java-queue-generator:latest

## Run

copy `config/config.yaml.sample` to `config/config.yaml`

edit the file to specify your topic id

copy `config/queue-config.yaml.sample` to `config/queue-config.yaml`

edit the file to match your rabbit mq configuration (defaults as they are will work out of the box)
Also specify message `iterations` and `delayMillis` between messages.

copy `config/dotenv.sample` to `config/.env`

edit the file to include your `OPERATOR_ID` and `OPERATOR_KEY`

Then run the docker images `docker-compose up --remove-orphans`

## Individual components

See sections below for individual build/run instructions for each component

## hcs-sxc-java-queue-generator

This java project generates random XML messages and sends them to a rabbit MQ server.

Example message: 
```
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<sample>
   <someText lorem="omittam deseruisse dico id eloquentiam ridens praesent"/>
</sample>
```

### Build

Switch to the project's folder and run

`./mvnw clean install`

### Run

Ensure a Rabbit MQ instance is available and running and that you have the necessary credentials for it.

copy `config/config.yaml.sample` to `config/config.yaml`

edit `config/config.yaml` to match your requirements

run `java -jar hcs-sxc-java-queue-generator-0.0.3-SNAPSHOT-run.jar`

use `CTRL + C` to stop if you have specified infinite iterations

*Note: changes to the `config.yaml` file are taken into account dynamically so you can update the host, port or credentials in the event of a failure to connect without needing to restart the image, it will keep trying with 10s intervals until it succeeds or is stopped*

### Docker

The maven build will generate a docker image `hederahashgraph/hcs-sxc-java-queue-generator:latest`.
This image will only work if a `config` folder containing a `config.yaml` file is mounted to the image at the `/config` location.
 
## hcs-sxc-java-queue-listener

Listens to messages from MQ and pushes them to HCS.

### Build

Switch to the project's folder and run

`./mvnw clean install`

### Run

copy `config/config.yaml.sample` to `config/config.yaml`

edit the file to specify your topic id

copy `config/queue-config.yaml.sample` to `config/queue-config.yaml`

edit the file to match your rabbit mq configuration (defaults as they are will work out of the box)

*Note `iterations` and `delayMillis` are not applicable to this component*

copy `config/dotenv.sample` to `config/.env`

edit the file to include your `OPERATOR_ID` and `OPERATOR_KEY`

run `java -jar target/hcs-sxc-java-queue-listener-0.0.3-SNAPSHOT-run.jar`

### Docker

The maven build will generate a docker image `hederahashgraph/hcs-sxc-java-queue-listener:latest`.
This image will only work if a `config` folder containing `config.yaml`, `queue-config.yaml` and `.env` files is mounted to the image at the `/config` location. 

## Queue Server

When running `docker-compose up` the accompanied `Dockerfile` is consulted to build a custom rabbit-mq image which enables the stomp protocol and web management; the latter is accessible under `localhost:9090` (credentials `guest/guest`). 
See local README.md for instructions on how to run rabbit-mq without `docker-compose`.

## Web STOMP clients
copy `webapp/config.js.sample` to `webapp/config.js` and click on `index.html` and `index2.html`. The files will be served from disk and should open in a browser automaticaly . Messages entered in `index.html` will be sent to HCS and the resulting HCS output will appear in `index2.html`.
