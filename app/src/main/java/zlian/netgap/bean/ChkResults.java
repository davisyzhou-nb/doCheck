package zlian.netgap.bean;

public class ChkResults {
    String result;
    int index;
    public ChkResults(String result, int index) {
        this.result = result;
        this.index = index;
    }
    public String getResult() {
        return result;
    }

    public int getIndex() {
        return index;
    }
}