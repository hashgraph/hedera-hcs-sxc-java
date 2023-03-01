# HCS-SXC-Java-Settlement-Demo

Running in docker containers

## Setting up

### Clone the repository

```shell
git clone https://github.com/hashgraph/hedera-hcs-sxc-java.git
```

### Setup the environment

```shell
cd hedera-hcs-sxc-java/hcs-sxc-java-examples/hcs-sxc-java-settlement-demo
cp config/dotenv.sample config/.env
cp config/config.yaml.sample config/config.yaml
```

### Create an HCS topic



### Edit `config/.env`

complete the values for `OPERATOR_ID` and `OPERATOR_KEY` from the Hedera portal (for testnet)

### Edit `config/config.yaml`

input your `topicId` against the `topic` entry

```
topic: 0.0.12341234
```

you may also change values for `signMessages`, `encryptMessages`, `rotateKeys` and `rotateKeyFrequency`

### Build the docker image

```shell
./build.sh
```

## Start the docker containers

```shell
./run.sh
```

## Stop the docker containers

```shell
./stop.sh
```
