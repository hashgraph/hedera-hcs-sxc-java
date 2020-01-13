package com.hedera.hcs.sxc.interfaces;

import org.apache.commons.lang3.tuple.Pair;

public interface SxcKeyRotation {
    public byte[] initiate() throws Exception;
    public static Pair<byte[],byte[]> respond(byte[] initiatorPubKeyEnc) throws Exception {
        return Pair.of(new byte[0], new byte[0]);
    };
    public byte[] finalise(byte[] responderPubKeyEnc) throws Exception;
}
