package zlian.netgap.mtool;

public class Data {
    long timestamp;
    byte[] data;
    DataStatus status=DataStatus.UNKNOW;

    public Data(){}

    public Data(long timestamp, byte[] data) {
        this.timestamp = timestamp;
        this.data = data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public DataStatus getStatus() {
        return status;
    }

    public void setStatus(DataStatus status) {
        this.status = status;
    }
}
