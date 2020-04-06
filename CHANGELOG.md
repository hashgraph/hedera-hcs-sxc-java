# Changelog

Besides bug fixes, some features may have changed with this release which need your attention, these will be listed here.

## April 6th 2020

* It is now possible to override encryption in code (this is not longer entirely dictated by the config.yaml flag)

## March 25th 2020

* Proof after the fact. Outbound messages provide a `prove (originalMessage, publicKey)` api call, which allows sending proof requests to all peers from the address-book. Peers can verify messages by inspecting their messages received from the mirror and verify messages even if the stored messages are in encrypted form; thus, no decryption key is required to verify a message.

* The simple demo demonstrates proof after the fact.  

* The simple demo supports simple message thread creation, such that conversations are grouped by named threads. This demonstrates simple  state management and state replication across multiple App net participants.  


## March 6th 2020

* Pair-wise encryption. Both demos support now address-book key-lookup to encrypt and sign messages destined to targeted recipients where communicating pairs use shared keys. In the simple-message-demo you may notice a message echoing back several times when the message is sent out to several recipients. Similarly, in the settlement demo you may notice repeated application messages, with distinct sequence numbers, in the audit log.

Additionally, running the demos with encryption now requires a `contact-list.yaml` file to be generated. An automatic generator is available in both demos, please refer to `README.md` for details.

## Feburary 18th 2020

* App Ids are now optional strings, requires an update to docker-compose.

## February 4th 2020

* Cryptography plug in for encryption/decryption

## February 4th 2020

* Demo now supports fully automatic processing for credits and settlements (individually selectable). If a credit or settlement is set to automatic in the UI, the demo will proceed without further user input.

* Control returned to user immediately after closing new credit dialog allowing for a new credit to be created without waiting for mirror node response.

## February 3rd 2020

Simple demo simplification. It is now possible to run simple demo without relay and queue components. See README.md for details.
