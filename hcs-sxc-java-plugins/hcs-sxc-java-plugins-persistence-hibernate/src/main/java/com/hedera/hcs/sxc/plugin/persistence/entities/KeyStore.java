package com.hedera.hcs.sxc.plugin.persistence.entities;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name = "KeyStore")
public class KeyStore implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    @Id
    private int id;
    @Lob
    private byte[] publicKey;
    @Lob
    private byte[] secretKey;
}
