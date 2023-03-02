FROM adoptopenjdk:14-jdk AS build

# Install git
RUN apt-get update
RUN apt-get -y install gnupg
RUN apt-key adv --keyserver keyserver.ubuntu.com --recv-keys 648ACFD622F3D138
RUN apt-key adv --keyserver keyserver.ubuntu.com --recv-keys 0E98404D386FA1D9
RUN apt-get -y install git

# Fetch the code
RUN git clone https://github.com/hashgraph/hedera-hcs-sxc-java.git
RUN cd /hedera-hcs-sxc-java && git fetch
RUN cd /hedera-hcs-sxc-java && git checkout settlement-docker

# Remove config folder (it's mapped as a volume)
RUN rm -rf /hedera-hcs-sxc-java/hcs-sxc-java-examples/hcs-sxc-java-settlement-demo/config

# Compile
RUN cd /hedera-hcs-sxc-java && ./mvnw install -P examples-settlement,h2

FROM adoptopenjdk:14-jdk

COPY --from=build /hedera-hcs-sxc-java /hedera-hcs-sxc-java

CMD ["./hedera-hcs-sxc-java/hcs-sxc-java-examples/hcs-sxc-java-settlement-demo/config/runApp.sh"]
