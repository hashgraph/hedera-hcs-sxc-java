# hcs-sxc-java-token-demo

This demo shows how a token can be built on HCS. A Java back end is run for each node within an app net and users can interact with the app net nodes via a web UI.

The demo runs up to three nodes (CBDC, TrustCorp and GrandCredit), each has a set of pre-defined users

* CDBC (Controller and Junior Controller) 
* GrandCredit (Alice, Bob and GrandCredit Manager)
* TrustCorp (Carlos, Dave and TrustCorp Manager)

## Java App Net Nodes

### Build

Compile the parent project (hedera-hcs-sxc-java) with `./mvwn clean install`, this will also compile the demo.

### Configuration

Copy `config/dotenv.sample` to `config/default.env`
Edit `default.env` to include your network `OPERATOR_ID` and `OPERATOR_KEY`

Copy `config/config.yaml.sample` to `config/config.yaml`
Edit the `topic: 0.0.999` to match a topic ID you created

### Running

Open three separate terminal windows and run

```
cd target
./hcs-sxc-java-token-demo-0.0.3-SNAPSHOT -app.user=CBDC --app.port=8080 --app.pubkey=pubkey
```

```
cd target
./hcs-sxc-java-token-demo-0.0.3-SNAPSHOT -app.user=GrandCredit --app.port=8081 --app.pubkey=pubkey
```

```
cd target
./hcs-sxc-java-token-demo-0.0.3-SNAPSHOT -app.user=TrustCorp --app.port=8082 --app.pubkey=pubkey
```

This will start your three app net nodes.

## Web UI
This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 9.1.0.

 Download dependencies
```
cd js
npm install
```

To specify which app net node the UI should point to, edit `js/src/env.js` and update the `window.__env.javaPort = '8080';` line to match the port the app net node is listening on (8080, 8081 or 8082)

```
cd js
ng serve
```

Navigate to `http://localhost:4200` in a browser.

## Build for deployment

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory. Use the `--prod` flag for a production build.

## Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI README](https://github.com/angular/angular-cli/blob/master/README.md).
