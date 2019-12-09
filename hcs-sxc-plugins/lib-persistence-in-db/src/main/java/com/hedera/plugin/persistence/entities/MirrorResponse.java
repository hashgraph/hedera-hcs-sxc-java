package com.hedera.plugin.persistence.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name = "MirrorResponses")
public class MirrorResponse {
    @Id
    private String timestamp;
    private byte[] mirrorTopicMessageResponse;
}
