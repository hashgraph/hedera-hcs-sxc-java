# HCS-SXC Example Projects

The project comes with several examples to get you started, these are fully functional examples.  

## Compiling and Running the examples

For the examples to work you need to make sure that runtime configuration files of each example are complete and accurate. Configuration files are consulted only at runtime and are available in each example project. Consult the documentation of each example for setting up configuration files.  If you built the entire project at the root with `mvn clean install` then all examples will compile. It is preferable to build each example individually but if you want to build it all then  ensure the necessary configuration files are complete and accurate (use provided samples as starting points)

- hcs-sxc-java-examples/hcs-sxc-java-settlement-demo/config/.env
- hcs-sxc-java-examples/hcs-sxc-java-settlement-demo/config/.config.yaml
- hcs-sxc-java-examples/hcs-sxc-java-settlement-demo/config/docker-compose.yml

- hcs-sxc-java-examples/hcs-sxc-java-simple-message-demo/config/apps.yaml
- hcs-sxc-java-examples/hcs-sxc-java-simple-message-demo/config/config.yaml

- hcs-sxc-java-examples/hcs-sxc-java-simple-message-demo/docker-compose.yml (only if you want to try simple demo through relay and queue)
- hcs-sxc-java-examples/hcs-sxc-java-simple-message-demo/config/queue-config.yaml (only if you want to try simple demo through relay and queue)
- hcs-sxc-java-examples/hcs-sxc-java-simple-message-demo/config/relay-config.yaml (only if you want to try simple demo through relay and queue)

## Overview

The first `hcs-sxc-java-simple-message-demo` is a simple command line example where running two instances of the application side by side, you can witness that a message sent from one app is reflected in the other. The first app sends the message to Hedera and the second receives it via a subscription to a mirror node. The opposite also works. The second example `hcs-sxc-java-settlement-demo` is a more complex application which is based on spring boot with a web UI. Each instance of the application represents a participant in a settlement use case where participants can issue credit notes to each other, approve them, group them to reach a settlement amount, employ a third party to effect the payment and finally both original parties confirm the payment was completed. In addition to this, an audit log is provided so that the full history of messages between participants can be consulted. The third example is a token on HCS demo. Lastly, we have two examples that demonstrate `logging` of various services; the examples show how to monitor `rabbit-mq`  and `cloudwatch` messages; the former  includes a web-ui that is akin to a instant messenger where each message is timestamped by HCS. 

## List of projects

Select a demo to read compilation instructions. 

- [Simple message demo](./hcs-sxc-java-simple-message-demo/README.md)  
  - Java console application
  - Maintaining simple state 
  - Requesting proof after the fact
  - Address book management and pair-wise encryption. 
- [Financial Settlement](./hcs-sxc-java-settlement-demo/README.md) 
  - Advanced UX showcase with reactive web interface
  - Multiple user roles with complex state machine management
  - Building HCS app nets with  REST and WebSocket endpoints
- [Listening Message Queues with logging and  timestamping](./hcs-sxc-java-mq-consumer/README.md)
  - Support for RabbitMQ
  - Support for Kafka
  - MQ Instant messenger demo with Web UI
- [Listening and logging Amazon Cloudwatch events](./hcs-sxc-java-cloudwatch/README.md)

- [Token demo](./hcs-sxc-java-token-demo/README.md)