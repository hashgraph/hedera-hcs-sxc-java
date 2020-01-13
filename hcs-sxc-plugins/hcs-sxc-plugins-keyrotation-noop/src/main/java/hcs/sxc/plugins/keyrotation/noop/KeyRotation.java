package hcs.sxc.plugins.keyrotation.noop;

import org.apache.commons.lang3.tuple.Pair;

import com.hedera.hcs.sxc.interfaces.SxcKeyRotation;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class KeyRotation implements SxcKeyRotation {

    /**
     * Constructs an object for the key rotation initiator to keep and reuse 
     * the generated KeyAgreement. 
     * Note that the key rotation responder uses a static method to interact with
     * this class while the initiator needs to construct an object. 
     */ 
    public KeyRotation() {
    }

    @Override
    public byte[] initiate() throws Exception {
        log.info("initiate");
        return new byte[0];
    }

    public static Pair<byte[],byte[]> respond(byte[] initiatorPubKeyEnc) throws Exception {
        log.info("respond");
        return Pair.of(new byte[0], new byte[0]);
    }

    @Override
    public byte[] finalise(byte[] responderPubKeyEnc) throws Exception {
        log.info("finalise");
        return new byte[0];
    }
 }
