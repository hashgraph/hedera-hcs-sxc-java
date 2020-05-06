# hcs-sxc-java-queue-consumer

Consumes messages from a variety of message queues (MQ, Google or Amazon SQS) and pushes the messages to HCS. Upon receipt of a notification from HCS, the HCS response can optionally be sent to another queue.

A generator is included in this project for testing purposes.

## Build

run `./mvnw clean install`, this will compile all the elements of the project and generate docker images for the listener and generator.

- hederahashgraph/hcs-sxc-java-queue-listener:latest
- hederahashgraph/hcs-sxc-java-queue-generator:latest

## Run

copy `config/config.yaml.sample` to `config/config.yaml`

edit the file to specify your topic id

copy `config/queue-config.yaml.sample` to `config/queue-config.yaml`

edit the file to match your message queue configuration
Also specify message `iterations` and `delayMillis` between messages.

copy `config/dotenv.sample` to `config/.env`

edit the file to include your `OPERATOR_ID` and `OPERATOR_KEY`

### Google PubSub configuration

The necessary credentials for Google PubSub must be stored in a JSON file and the location of the file specified in the `GOOGLE_APPLICATION_CREDENTIALS` environment variable.
If you're using Google PubSub, edit the `docker-compose.yml` file to specify the location of the file (for both the listener and generator). It is recommended you place the file in the `config` folder and reference the file as `./config/credentials.json` since the `config` folder is mounted on the docker containers.

### Amazon SQS configuration

The necessary credentials for Amazon SQS must be provided via environment variables as follows:

```
AWS_ACCESS_KEY_ID
AWS_REGION
AWS_SECRET_ACCESS_KEY
```

If you're using Amazon SQS, edit the `docker-compose.yml` file to specify the environment variables for both the listener and the generator.

## Docker

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

### Google PubSub configuration

The necessary credentials for Google PubSub must be stored in a JSON file and the location of the file specified in the `GOOGLE_APPLICATION_CREDENTIALS` environment variable.
If you're using Google PubSub, specify the location of the credentials file in this environment variable before running the application.

### Amazon SQS configuration

The necessary credentials for Amazon SQS must be provided via environment variables as follows:

```
AWS_ACCESS_KEY_ID
AWS_REGION
AWS_SECRET_ACCESS_KEY
```

If you're using Amazon SQS, be sure to set the environment variables to the appropriate values.

### Execute

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

### Google PubSub configuration

The necessary credentials for Google PubSub must be stored in a JSON file and the location of the file specified in the `GOOGLE_APPLICATION_CREDENTIALS` environment variable.
If you're using Google PubSub, specify the location of the credentials file in this environment variable before running the application.

### Execute

run `java -jar target/hcs-sxc-java-queue-listener-0.0.3-SNAPSHOT-run.jar`

### Docker

The maven build will generate a docker image `hederahashgraph/hcs-sxc-java-queue-listener:latest`.
This image will only work if a `config` folder containing `config.yaml`, `queue-config.yaml` and `.env` files is mounted to the image at the `/config` location. 

## Queue Server

When running `docker-compose up` the accompanied `Dockerfile` is consulted to build a custom rabbit-mq image which enables the stomp protocol and web management; the latter is accessible under `localhost:9090` (credentials `guest/guest`). 
See local README.md for instructions on how to run rabbit-mq without `docker-compose`.

## Web STOMP clients
copy `webapp/config.js.sample` to `webapp/config.js` and click on `alice.html`, `bob.html` and `carol.html`. 
The files will be served from disk and should open in a browser automaticaly . Messages entered in `alice.html` or `bob.html`  will be sent to each other, via the MQ, and to HCS for timestamping. The resulting HCS output will appear in `carol.html`.
