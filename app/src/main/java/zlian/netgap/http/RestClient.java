package zlian.netgap.http;

import android.content.Context;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import zlian.netgap.app.Constants;
import zlian.netgap.support.HttpUtil;

public class RestClient implements Constants {

	public static void cancel(Context context, boolean interrupt) {
		HttpUtil.cancel(context, interrupt);
	}
	
	/**
	 * 获取最新的版本信息
	 * 
	 * @param
	 * @return
	 */
	public static void getLatestVersion(Context context, String appCode, String customer, AsyncHttpResponseHandler res) {
		String GETLATESTVERSION_URL = FIXED_URL + API_VER + customer + APPVERSION;

		RequestParams params = new RequestParams();
		params.put(POSTKEY_JSON, appCode);
		HttpUtil.get(context, GETLATESTVERSION_URL, params, res);
	}

}
