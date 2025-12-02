package zlian.netgap.data;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import zlian.netgap.bean.CheckPoint;
import zlian.netgap.mtool.BtService;
import zlian.netgap.support.FileUtil;
import zlian.netgap.util.SDCardUtil;

public final class TempData {

    private static  TempData ins = new TempData();

    public static String uuid = UUID.randomUUID().toString();

    private List<CheckPoint> checkPointList = new ArrayList<CheckPoint>();
    private BtService mChatService = new BtService(null,null);
    private long timestamp = 0;
    private String zipCompressedChkList = "";

    private String choosedAddress = ""; // connect to device
    private String deviceName = "";

    public String getChoosedAddress() {
        return choosedAddress;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setChoosedAddress(String choosedAddress) {
        this.choosedAddress = choosedAddress;
    }

    public String getZipCompressedChkList() {
        return zipCompressedChkList;
    }

    public void setZipCompressedChkList(String zipCompressedChkList) {
        this.zipCompressedChkList = zipCompressedChkList;
    }

    public void addZipCompressedChkList(String zipCompressedChkList) {
        this.zipCompressedChkList += zipCompressedChkList;
    }

    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public static TempData getIns() {
        Log.e("aaa", SDCardUtil.checkSDCardAvailable() + "");
        boolean a = SDCardUtil.checkSDCardAvailable();
        FileUtil.checkpath(FileUtil.filePath);
        FileUtil.checkpath(FileUtil.zipPath);
        FileUtil.checkpath(FileUtil.filePath+uuid+"/");
        return ins;
    }

    public List<CheckPoint> getCheckPointList() {
        return checkPointList;
    }

    public void setCheckPointList(List<CheckPoint> checkPointList) {
        this.checkPointList.clear();
        this.checkPointList.addAll(checkPointList);
    }

    public void removeAllPointList(){
        this.checkPointList.clear();
    }

    public CheckPoint getCheckPoint(int index){
        return checkPointList.get(index);
    }

    public int size(){
        return this.checkPointList.size();
    }

    public BtService getBtService() { return mChatService;}
    public void reNewBtService() {
        mChatService = new BtService(null,null);
    }
}
