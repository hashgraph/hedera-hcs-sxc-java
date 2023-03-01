#FROM openjdk:10.0.2-13
FROM openjdk:10.0.2-13 AS build

# Install git
RUN apt-key adv --keyserver keyserver.ubuntu.com --recv-keys 648ACFD622F3D138
RUN apt-key adv --keyserver keyserver.ubuntu.com --recv-keys 0E98404D386FA1D9
RUN apt-get -y install git

# Fetch the code
RUN git clone https://github.com/hashgraph/hedera-hcs-sxc-java.git

# Copy reduced POM to speed up compilation
COPY docker/core-pom-reduced.xml /hedera-hcs-sxc-java/pom.xml
COPY docker/examples-pom-reduced.xml /hedera-hcs-sxc-java/hcs-sxc-java-examples/pom.xml
# Remove config folder (it's mapped as a volume)
RUN rm -rf /hedera-hcs-sxc-java/hcs-sxc-java-examples/hcs-sxc-java-settlement-demo/config

# Compile
RUN cd /hedera-hcs-sxc-java && ./mvnw install

FROM openjdk:10.0.2-13

COPY --from=build /hedera-hcs-sxc-java /hedera-hcs-sxc-java

CMD ["./hedera-hcs-sxc-java/hcs-sxc-java-examples/hcs-sxc-java-settlement-demo/config/runApp.sh"]
