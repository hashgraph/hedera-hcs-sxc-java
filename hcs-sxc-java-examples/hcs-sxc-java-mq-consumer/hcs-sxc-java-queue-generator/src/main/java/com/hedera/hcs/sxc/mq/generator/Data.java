package com.hedera.hcs.sxc.mq.generator;

import com.thedeanda.lorem.Lorem;
import com.thedeanda.lorem.LoremIpsum;
import org.apache.commons.codec.digest.DigestUtils;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.apache.commons.codec.digest.MessageDigestAlgorithms.SHA_384;

public class Data {
    private static final Lorem lorem = LoremIpsum.getInstance();
    public static String getRandomData() throws NoSuchAlgorithmException {

        String words = lorem.getWords(5, 10);
        // generate SHA 384 hash of words
        MessageDigest md = MessageDigest.getInstance("SHA-384");
        byte[] messageDigest = md.digest(words.getBytes());
        BigInteger no = new BigInteger(1, messageDigest);
        String hashtext = no.toString(16);
        while (hashtext.length() < 32) {
            hashtext = "0" + hashtext;
        }
        String xml = "<?xml version=\"1.0\"?>";
        xml = xml.concat("<sampleData>");
        xml = xml.concat("<data>").concat(words).concat("</data>");
        xml = xml.concat("<hash>").concat(hashtext).concat("</hash>");
        xml = xml.concat("</sampleData>");

        return xml;
    }
}
