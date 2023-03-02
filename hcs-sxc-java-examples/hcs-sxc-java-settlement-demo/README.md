# HCS-SXC-Java-Settlement-Demo

Running in docker containers

## Setting up

### Clone the repository

```shell
git clone https://github.com/hashgraph/hedera-hcs-sxc-java.git
```

### Build the docker image

```shell
./build.sh
```

### Setup the environment

note, the `config` folder is a volume for the docker images, so changes made there will take effect whenever you restart the containers.

```shell
cd hedera-hcs-sxc-java/hcs-sxc-java-examples/hcs-sxc-java-settlement-demo
cp config/dotenv.sample config/.env
cp config/config.yaml.sample config/config.yaml
```

### Edit `config/.env`

complete the values for `OPERATOR_ID` and `OPERATOR_KEY` from the Hedera portal (for testnet)

### Create an HCS topic

Note, you can create a topic using one of the SDKs, etc... however, it is possible to do so with the docker image once built.

```shell
./createTopic.sh
```

will output a new topic id in your terminal (e.g. `0.0.3616311`)

_note: you may get a `[0.004s][error][cds] Unable to map CDS archive -- os::vm_allocation_granularity() expected: 65536 actual: 4096` on Apple Silicon Macs, this doesn't appear to be problematic, the topic creation still works._

### Edit `config/config.yaml`

input your `topicId` against the `topic` entry

```
topic: 0.0.12341234
```

you may also change values for `signMessages`, `encryptMessages`, `rotateKeys` and `rotateKeyFrequency`

## Start the docker containers

```shell
./run.sh
```

## Stop the docker containers

```shell
./stop.sh
```

### Navigate to the demo UI

The docker-compose spins up four containers as follows

* Alice on port 8081 (http://localhost:8081)
* Bob on port 8082 (http://localhost:8082)
* Diana on port 8084 (http://localhost:8084)
* Grace on port 8087 (http://localhost:8087)


