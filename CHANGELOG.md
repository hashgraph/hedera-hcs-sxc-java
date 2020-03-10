# Changelog

Besides bug fixes, some features may have changed with this release which need your attention, these will be listed here.

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

