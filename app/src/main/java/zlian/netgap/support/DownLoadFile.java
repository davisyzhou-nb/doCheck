package zlian.netgap.support;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.webkit.URLUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DownLoadFile {

    public final static String TAG = "DownLoadFile";

    public void downLoadFile(final Activity activity, final String filePath,
                             final String fileEx) {
        try {
            Runnable r = new Runnable() {
                public void run() {
                    try {
                        File tempFile = getDataSource(filePath, fileEx);
                        openFile(activity, tempFile);
                        activity.finish();
                    } catch (Exception e) {
                        //LogUtil.e(TAG,e.getMessage());
                    }
                }
            };
            new Thread(r).start();
        } catch (Exception e) {
            //LogUtil.e(TAG,e.getMessage());
        }
    }

    private File getDataSource(String filePath, String fileEx) throws Exception {
        File myTempFile = null;

        if (!URLUtil.isNetworkUrl(filePath)) {
            //LogUtil.d(TAG,"url wrong");

        } else {
            URL myURL = new URL(filePath);
            HttpURLConnection conn = (HttpURLConnection) myURL.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();

            if (is == null) {
                throw new RuntimeException("stream is null");
            }
            myTempFile = new File(FileUtil.FILEPATH);
            String fileName = new SimpleDateFormat("yyyyMMddHHmmss")
                    .format(new Date())
                    + fileEx;

            if (!myTempFile.exists()) {

                myTempFile.mkdirs();
            }
            myTempFile = new File(FileUtil.FILEPATH, fileName);
            FileOutputStream fos = new FileOutputStream(myTempFile);
            byte buf[] = new byte[8192];
            do {
                int numread = is.read(buf);
                if (numread <= 0) {
                    break;
                }

                fos.write(buf, 0, numread);
            } while (true);

            try {
                is.close();
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }

        return myTempFile;
    }

    private void openFile(Activity activity, File f) {

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        String type = getMIMEType(f);
        intent.setDataAndType(Uri.fromFile(f), type);
        activity.startActivity(intent);
    }

    private String getMIMEType(File f) {
        String type = "";
        String fName = f.getName();
        String end = fName
                .substring(fName.lastIndexOf(".") + 1, fName.length())
                .toLowerCase();

        if (end.equals("doc") || end.equals("dot")) {
            type = "application/msword";
        } else if (end.equals("xls")) {
            type = "application/vnd.ms-excel";
        } else if (end.equals("ppt")) {
            type = "application/vnd.ms-powerpoint";
        } else if (end.equals("pdf")) {
            type = "application/pdf";
        } else if (end.equals("rar")) {
            type = "application/x-rar-compressed";
        } else if (end.equals("zip")) {
            type = "application/zip";
        } else if (end.equals("bmp")) {
            type = "image/bmp";
        } else if (end.equals("gif")) {
            type = "image/gif";
        } else if (end.equals("jpg")) {
            type = "image/jpeg";
        } else if (end.equals("png")) {
            type = "image/png";
        } else if (end.equals("txt")) {
            type = "text/plain";
        } else if (end.equals("dotx")) {
            type = "application/vnd.openxmlformats-officedocument.wordprocessingml.template";
        } else if (end.equals("docx")) {
            type = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else if (end.equals("xlsx")) {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else if (end.equals("pptx")) {
            type = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        } else if (end.equals("apk")) {
            /* android.permission.INSTALL_PACKAGES */
            type = "application/vnd.android.package-archive";
        } else {
            type = "*/*";
        }
        return type;
    }

    public void delFile(File myFile) {
        if (myFile.exists()) {
            myFile.delete();
        }
    }

}
