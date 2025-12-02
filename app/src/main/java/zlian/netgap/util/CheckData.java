package zlian.netgap.util;

import zlian.netgap.mtool.Data;
import zlian.netgap.mtool.DataStatus;

public final class CheckData {

    private static  CheckData chkdat = new CheckData();
    private Data bufData; // 临时数据
    private static final int DATA_INTERVAL=500; // 500ms
    //private static final int DATA_INTERVAL=10000; // 10s

    public static CheckData getChkdat() {
        return chkdat;
    }

    public void setBufData(Data bufData) {
        this.bufData = bufData;
    }

    public Data getBufData() {
        return bufData;
    }
    /**
     * �����յ������
     * @param data
     * @return
     */
    public DataStatus validateData(final Data data){
        byte[] buf=data.getData();
        if (bufData==null ){
            if (buf==null){
                return DataStatus.BAD;
            }
            if (buf[0]!= '{'){
                return DataStatus.BAD;
            }
            int validated=validateJSON(buf);
            if (validated==0){
                return DataStatus.OK;
            }
            if (validated > 0){
                bufData=new Data(data.getTimestamp(),data.getData());
                bufData.setStatus(DataStatus.PENDING);
                return DataStatus.PENDING;
            }
            if (validated < 0){
                return DataStatus.BAD;
            }
        }else{
            if ((data.getTimestamp()- bufData.getTimestamp())>DATA_INTERVAL){
                bufData=null;
                return DataStatus.BAD;
            }
            if (bufData.getStatus()==DataStatus.PENDING){
                Data newData=new Data();
                newData.setTimestamp(data.getTimestamp());
                newData.setData(addAll(bufData.getData(),data.getData()));
                int validated=validateJSON(newData.getData());
                if (validated == 0){
                    bufData=newData;
                    bufData.setStatus(DataStatus.OK);
                    return DataStatus.OK;
                }else if (validated > 0 ){
                    bufData=newData;
                    bufData.setStatus(DataStatus.PENDING);
                    return DataStatus.PENDING;
                }
            }
        }
        bufData=null;
        return DataStatus.BAD;
    }

    private byte[] addAll(final byte[] first,final byte[] second){
        int len=first.length+second.length;
        byte[] newData=new byte[len];
        int firstLen=first.length;
        for (int i=0;i<firstLen;i++){
            newData[i]=first[i];
        }
        int secondLen=second.length;
        for(int i=0;i<secondLen;i++){
            newData[firstLen+i]=second[i];
        }
        return newData;
    }

    private int validateJSON(byte[] json){
        byte start='{';
        byte end='}';
        int counter=0;
        for (byte b: json){
            if (b==start){
                counter++;
            }else if(b==end){
                counter--;
            }
        }
        return counter;
    }
}
