# hcs-relay

A component of a HCS network which relays transactions from mirror node(s) to an hcs-queue

The relay listens to all incoming `TransactionRecord.class` 
 from mirror, selects the the ones where memo starts with 
`HCS` and cosntructs a `MessageChunk.class` 
from hcs-lib to forward to active-mq artemis
