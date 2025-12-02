package zlian.netgap.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import zlian.netgap.R;
import zlian.netgap.data.TempData;
import zlian.netgap.mtool.XZip;
import zlian.netgap.net.helper.ProgressHelper;
import zlian.netgap.net.listener.UIProgressListener;
import zlian.netgap.support.FileUtil;
import zlian.netgap.util.CommonFun;
import zlian.netgap.view.WheelProgressDialog;

public class UploadActivity extends CheckBaseActivity {

    private OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(1000, TimeUnit.MINUTES)
            .readTimeout(1000, TimeUnit.MINUTES)
            .writeTimeout(1000, TimeUnit.MINUTES)
            .build();
    int progress = 0;
    private Handler handler;
    private WheelProgressDialog wheelProgressDialog;
    private File photoFiles;
    private File zipFile;

    public static void startUpload(Context mContext, int page) {
        Intent intent = new Intent(mContext, UploadActivity.class);
        intent.putExtra(KEY_INDEX, page);
        mContext.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        handler = new Handler();
        wheelProgressDialog = new WheelProgressDialog(this);
        wheelProgressDialog.message("upload !").show();
        test();

        initViews();
        String path = FileUtil.filePath + TempData.uuid;
        photoFiles = new File(path);
        try {
            XZip.ZipFolder(path, FileUtil.zipPath + TempData.uuid + ".zip");
        } catch (Exception e) {
            e.printStackTrace();
        }
        upload(FileUtil.zipPath + TempData.uuid + ".zip");
    }

    @Override
    public void initViews() {

    }

    private void test() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
//                Looper.prepare();
                wheelProgressDialog.progress(progress).message(progress >= 100 ? getString(R.string.finished) : getString(R.string.img_uploading) + progress + "%");
                if (progress < 100) {
                    test();
                } else {
                    wheelProgressDialog.dismiss();
                    finish();
                    if (SummaryActivity.instance != null) {
                        SummaryActivity.instance.finish();
                    }
                    RecordActivity.startRecordActivity(mContext);
                }
//                Looper.loop();
            }
        }, 100);
    }

    private void upload(String path) {

        zipFile = new File(path);

        UIProgressListener uiProgressRequestListener = new UIProgressListener() {
            @Override
            public void onUIProgress(long bytesWrite, long contentLength, boolean done) {
                Log.e("TAG", "bytesWrite:" + bytesWrite);
                Log.e("TAG", "contentLength" + contentLength);
                Log.e("TAG", (100 * bytesWrite) / contentLength + " % done ");
                progress = (int) ((100 * bytesWrite) / contentLength);
                Log.e("TAG", "done:" + done);
                Log.e("TAG", "================================");
            }
        };

        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("devid", TempData.getIns().getDeviceName())
                .addFormDataPart("timestamp", String.valueOf(System.currentTimeMillis()))
                .addFormDataPart("file", zipFile.getName(), RequestBody.create(null, zipFile))
                .addPart(Headers.of("Content-Disposition", "form-data; name=\"another\";filename=\"another.dex\""), RequestBody.create(MediaType.parse("application/octet-stream"), zipFile))
                .build();


        try {
            Request request = new Request.Builder().url("http://cloud.teknect.cn/tm/api/v1/ejuser/inspectpic")
                    .post(ProgressHelper.addProgressRequestListener(requestBody, uiProgressRequestListener)).build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("TAG", "error ", e);
                    Looper.prepare();
                    Toast.makeText(getApplicationContext(), R.string.upload_failed, Toast.LENGTH_SHORT).show();
                    zipFile.delete();
                    CommonFun.delete(photoFiles);
                    Looper.loop();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.e("TAG", response.body().string());
                    Looper.prepare();
                    if (response.code() == 200) {
                        zipFile.delete();
                        CommonFun.delete(photoFiles);
                        Toast.makeText(getApplicationContext(), /*response.code()+*/R.string.upload_success, Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), /*response.code()+*/R.string.upload_failed, Toast.LENGTH_SHORT).show();
                        zipFile.delete();
                        CommonFun.delete(photoFiles);
                        progress = 100;
                    }
                    Looper.loop();
                }
            });
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(), R.string.upload_failed_internet, Toast.LENGTH_SHORT).show();
            zipFile.delete();
            CommonFun.delete(photoFiles);
            progress = 100;
        }
    }

}
