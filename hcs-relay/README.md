# hcs-relay

A component of a HCS network which relays transactions from mirror node(s) to an hcs-queue

The relay listens to incoming Payment Transfers from mirror, selects the the ones where memo 
starts with `HCS`, clips these 3 lettres from memo and with the tail he cosntructs a `MessageChunk.class` 
from hcs-lib to forward to active-mq artemis 
