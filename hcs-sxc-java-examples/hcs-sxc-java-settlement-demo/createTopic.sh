#!/bin/sh

#docker run --mount type=bind,source="$(pwd)"/config,target=/hedera-hcs-sxc-java/hcs-sxc-java-examples/hcs-sxc-java-settlement-demo/config settlement-demo:latest /hedera-hcs-sxc-java/hcs-sxc-java-examples/hcs-sxc-java-settlement-demo/config/createTopic.sh
docker run --mount type=bind,source="$(pwd)"/config,target=/hedera-hcs-sxc-java/hcs-sxc-java-examples/hcs-sxc-java-settlement-demo/config settlement-demo:latest tail -f /dev/null
