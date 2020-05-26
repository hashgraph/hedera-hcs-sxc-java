

public final class Mq extends GenericConfig {
    private String host = "";
    private String user = "";
    private String password = "";
    private int port = 5672;

    public String getHost() {
        return this.host;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public String getUser() {
        return this.user;
    }
    public void setUser(String user) {
        this.user = user;
    }
    public String getPassword() {
        return this.password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public int getPort() { 
        return this.port;
    }
    public void setPort(int port) { 
        this.port = port; 
    }
}
