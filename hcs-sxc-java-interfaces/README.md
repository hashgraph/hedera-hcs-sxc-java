# Interfaces

A set of standard interfaces or structures for the various components to communicate with each other. Listed below are those that are used in the context of plug-ins which have to satisfy particular interface  requirements.

- HCSCallBackFromMirror - so that an app can register with the `hcs-sxc-java-core` for callbacks
- HCSCallBackToAppInterface - so that the `hcs-sxc-java-core` can call back to an app
- MirrorSubscriptionInterface - so that plugins can be made to subscribe to mirror notifications
- SxcPersistence - so that plugins can be used to persist data
- SxcAddressListItemCryptoInterface - so that an address book with participant keys is held by the core component
- SxcApplicationMessageInterface - so that application messages can be related to hcs consensus sequence numbers, running hashes and timestamps

Defined in the `hcs-sxc-java-Interfaces` project, these are data structures that are shared between components.

- HCSRelayMessage - a message from the `hcs-sxc-java-relay` components
- HCSResponse - a application message id and message
- SxcConsensusMessage - a (temporary) POJO for consensus messages (until these can be serialized)
- MessagePersistenceLevel - a list of pre-defined persistence levels