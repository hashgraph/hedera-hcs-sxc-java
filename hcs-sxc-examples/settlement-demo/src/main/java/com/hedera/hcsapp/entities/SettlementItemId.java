package com.hedera.hcsapp.entities;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Data;

@Data
@Embeddable
public class SettlementItemId implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Column(name = "settled_thread_id")
    private String settledThreadId;
 
    @Column(name = "thread_id")
    private String threadId;
 
    public SettlementItemId() {
    }
 
    public SettlementItemId(String settlementThreadId, String threadId) {
        this.settledThreadId = settlementThreadId;
        this.threadId = threadId;
    }
 
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SettlementItemId)) return false;
        SettlementItemId that = (SettlementItemId) o;
        return Objects.equals(getThreadId(), that.getThreadId()) &&
                Objects.equals(getSettledThreadId(), that.getSettledThreadId());
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(getSettledThreadId(), getThreadId());
    }

}
