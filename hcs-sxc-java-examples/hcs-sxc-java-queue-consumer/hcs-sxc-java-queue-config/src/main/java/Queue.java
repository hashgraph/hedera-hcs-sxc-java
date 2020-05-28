

public final class Queue {
    private int iterations = 0;
    private int delayMillis = 2;
    private Pubsub pubsub;
    private Sqs sqs;
    private Mq mq;
    private Kafka kafka;
    
    public int getIterations() {
        return this.iterations;
    }
    public void setIterations(int iterations) {
        this.iterations = iterations;
    }
    public int getDelayMillis() {
        return this.delayMillis;
    }
    public void setDelayMillis(int delayMillis) { 
        this.delayMillis = delayMillis; 
    }
    public Pubsub getPubSub() {
        return this.pubsub;
    }
    public void setPubsub(Pubsub pubsub) {
        this.pubsub = pubsub;
    }
    public Sqs getSqs() {
        return this.sqs;
    }
    public void setSqs(Sqs sqs) {
        this.sqs = sqs;
    }
    public Mq getMq() {
        return this.mq;
    }
    public void setMq(Mq mq) {
        this.mq = mq;
    }
    public Kafka getKafka() {
        return this.kafka;
    }
    public void setKafka(Kafka kafka) {
        this.kafka = kafka;
    }
}
