package zlian.netgap.support;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

public class JsonUtils {

	private final static String TAG = "JsonUtils";

	public static final String EMPTY = "";
	/** 绌虹殑 {@code JSON} 鏁版嵁 - <code>"{}"</code>銆� */
	public static final String EMPTY_JSON = "{}";
	/** 绌虹殑 {@code JSON} 鏁扮粍(闆嗗悎)鏁版嵁 - {@code "[]"}銆� */
	public static final String EMPTY_JSON_ARRAY = "[]";
	/** 榛樿鐨� {@code JSON} 鏃ユ湡/鏃堕棿瀛楁鐨勬牸寮忓寲妯″紡銆� */
	public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss SSS";
	/** {@code Google Gson} 鐨� {@literal @Since} 娉ㄨВ甯哥敤鐨勭増鏈彿甯搁噺 - {@code 1.0}銆� */
	public static final Double SINCE_VERSION_10 = 1.0d;
	/** {@code Google Gson} 鐨� {@literal @Since} 娉ㄨВ甯哥敤鐨勭増鏈彿甯搁噺 - {@code 1.1}銆� */
	public static final Double SINCE_VERSION_11 = 1.1d;
	/** {@code Google Gson} 鐨� {@literal @Since} 娉ㄨВ甯哥敤鐨勭増鏈彿甯搁噺 - {@code 1.2}銆� */
	public static final Double SINCE_VERSION_12 = 1.2d;

	/**
	 * 灏嗙粰瀹氱殑鐩爣瀵硅薄鏍规嵁鎸囧畾鐨勬潯浠跺弬鏁拌浆鎹㈡垚 {@code JSON} 鏍煎紡鐨勫瓧绗︿覆銆�
	 * <p />
	 * <strong>璇ユ柟娉曡浆鎹㈠彂鐢熼敊璇椂锛屼笉浼氭姏鍑轰换浣曞紓甯搞�傝嫢鍙戠敓閿欒鏃讹紝鏇鹃�氬璞¤繑鍥� <code>"{}"</code>锛� 闆嗗悎鎴栨暟缁勫璞¤繑鍥�
	 * <code>"[]"</code></strong>
	 * 
	 * @param target
	 *            鐩爣瀵硅薄銆�
	 * @param targetType
	 *            鐩爣瀵硅薄鐨勭被鍨嬨��
	 * @param isSerializeNulls
	 *            鏄惁搴忓垪鍖� {@code null} 鍊煎瓧娈点��
	 * @param version
	 *            瀛楁鐨勭増鏈彿娉ㄨВ銆�
	 * @param datePattern
	 *            鏃ユ湡瀛楁鐨勬牸寮忓寲妯″紡銆�
	 * @param excludesFieldsWithoutExpose
	 *            鏄惁鎺掗櫎鏈爣娉� {@literal @Expose} 娉ㄨВ鐨勫瓧娈点��
	 * @return 鐩爣瀵硅薄鐨� {@code JSON} 鏍煎紡鐨勫瓧绗︿覆銆�
	 */
	public static String toJson(Object target, Type targetType,
			boolean isSerializeNulls, Double version, String datePattern,
			boolean excludesFieldsWithoutExpose) {
		if (target == null)
			return EMPTY_JSON;
		GsonBuilder builder = new GsonBuilder();
		if (isSerializeNulls)
			builder.serializeNulls();
		if (version != null)
			builder.setVersion(version.doubleValue());
		if (isEmpty(datePattern))
			datePattern = DEFAULT_DATE_PATTERN;
		builder.setDateFormat(datePattern);
		if (excludesFieldsWithoutExpose)
			builder.excludeFieldsWithoutExposeAnnotation();
		String result = EMPTY;
		Gson gson = builder.create();
		try {
			if (targetType != null) {
				result = gson.toJson(target, targetType);
			} else {
				result = gson.toJson(target);
			}
		} catch (Exception ex) {
			Log.e(TAG, "鐩爣瀵硅薄 " + target.getClass().getName()
					+ " 杞崲 JSON 瀛楃涓叉椂锛屽彂鐢熷紓甯革紒");
			// log.warn("鐩爣瀵硅薄 " + target.getClass().getName()
			// + " 杞崲 JSON 瀛楃涓叉椂锛屽彂鐢熷紓甯革紒", ex);
			if (target instanceof Collection || target instanceof Iterator
					|| target instanceof Enumeration
					|| target.getClass().isArray()) {
				result = EMPTY_JSON_ARRAY;
			} else
				result = EMPTY_JSON;
		}
		return result;
	}

	/**
	 * 灏嗙粰瀹氱殑鐩爣瀵硅薄杞崲鎴� {@code JSON} 鏍煎紡鐨勫瓧绗︿覆銆�<strong>姝ゆ柟娉曞彧鐢ㄦ潵杞崲鏅�氱殑 {@code JavaBean}
	 * 瀵硅薄銆�</strong>
	 * <ul>
	 * <li>璇ユ柟娉曞彧浼氳浆鎹㈡爣鏈� {@literal @Expose} 娉ㄨВ鐨勫瓧娈碉紱</li>
	 * <li>璇ユ柟娉曚笉浼氳浆鎹� {@code null} 鍊煎瓧娈碉紱</li>
	 * <li>璇ユ柟娉曚細杞崲鎵�鏈夋湭鏍囨敞鎴栧凡鏍囨敞 {@literal @Since} 鐨勫瓧娈碉紱</li>
	 * <li>璇ユ柟娉曡浆鎹㈡椂浣跨敤榛樿鐨� 鏃ユ湡/鏃堕棿 鏍煎紡鍖栨ā寮� - {@code yyyy-MM-dd HH:mm:ss SSS}锛�</li>
	 * </ul>
	 * 
	 * @param target
	 *            瑕佽浆鎹㈡垚 {@code JSON} 鐨勭洰鏍囧璞°��
	 * @return 鐩爣瀵硅薄鐨� {@code JSON} 鏍煎紡鐨勫瓧绗︿覆銆�
	 */
	public static String toJson(Object target) {
		return toJson(target, null, false, null, null, true);
	}

	/**
	 * 
	 */
	// public static String toJson(Object target,TypeToken<T> token)
	// {
	// Gson gson = new Gson();
	// return gson.toJson(target);
	// }

	/**
	 * 灏嗙粰瀹氱殑鐩爣瀵硅薄杞崲鎴� {@code JSON} 鏍煎紡鐨勫瓧绗︿覆銆�<strong>姝ゆ柟娉曞彧鐢ㄦ潵杞崲鏅�氱殑 {@code JavaBean}
	 * 瀵硅薄銆�</strong>
	 * <ul>
	 * <li>璇ユ柟娉曞彧浼氳浆鎹㈡爣鏈� {@literal @Expose} 娉ㄨВ鐨勫瓧娈碉紱</li>
	 * <li>璇ユ柟娉曚笉浼氳浆鎹� {@code null} 鍊煎瓧娈碉紱</li>
	 * <li>璇ユ柟娉曚細杞崲鎵�鏈夋湭鏍囨敞鎴栧凡鏍囨敞 {@literal @Since} 鐨勫瓧娈碉紱</li>
	 * </ul>
	 * 
	 * @param target
	 *            瑕佽浆鎹㈡垚 {@code JSON} 鐨勭洰鏍囧璞°��
	 * @param datePattern
	 *            鏃ユ湡瀛楁鐨勬牸寮忓寲妯″紡銆�
	 * @return 鐩爣瀵硅薄鐨� {@code JSON} 鏍煎紡鐨勫瓧绗︿覆銆�
	 */
	public static String toJson(Object target, String datePattern) {
		return toJson(target, null, false, null, datePattern, true);
	}

	/**
	 * 灏嗙粰瀹氱殑鐩爣瀵硅薄杞崲鎴� {@code JSON} 鏍煎紡鐨勫瓧绗︿覆銆�<strong>姝ゆ柟娉曞彧鐢ㄦ潵杞崲鏅�氱殑 {@code JavaBean}
	 * 瀵硅薄銆�</strong>
	 * <ul>
	 * <li>璇ユ柟娉曞彧浼氳浆鎹㈡爣鏈� {@literal @Expose} 娉ㄨВ鐨勫瓧娈碉紱</li>
	 * <li>璇ユ柟娉曚笉浼氳浆鎹� {@code null} 鍊煎瓧娈碉紱</li>
	 * <li>璇ユ柟娉曡浆鎹㈡椂浣跨敤榛樿鐨� 鏃ユ湡/鏃堕棿 鏍煎紡鍖栨ā寮� - {@code yyyy-MM-dd HH:mm:ss SSS}锛�</li>
	 * </ul>
	 * 
	 * @param target
	 *            瑕佽浆鎹㈡垚 {@code JSON} 鐨勭洰鏍囧璞°��
	 * @param version
	 *            瀛楁鐨勭増鏈彿娉ㄨВ({@literal @Since})銆�
	 * @return 鐩爣瀵硅薄鐨� {@code JSON} 鏍煎紡鐨勫瓧绗︿覆銆�
	 */
	public static String toJson(Object target, Double version) {
		return toJson(target, null, false, version, null, true);
	}

	/**
	 * 灏嗙粰瀹氱殑鐩爣瀵硅薄杞崲鎴� {@code JSON} 鏍煎紡鐨勫瓧绗︿覆銆�<strong>姝ゆ柟娉曞彧鐢ㄦ潵杞崲鏅�氱殑 {@code JavaBean}
	 * 瀵硅薄銆�</strong>
	 * <ul>
	 * <li>璇ユ柟娉曚笉浼氳浆鎹� {@code null} 鍊煎瓧娈碉紱</li>
	 * <li>璇ユ柟娉曚細杞崲鎵�鏈夋湭鏍囨敞鎴栧凡鏍囨敞 {@literal @Since} 鐨勫瓧娈碉紱</li>
	 * <li>璇ユ柟娉曡浆鎹㈡椂浣跨敤榛樿鐨� 鏃ユ湡/鏃堕棿 鏍煎紡鍖栨ā寮� - {@code yyyy-MM-dd HH:mm:ss SSS}锛�</li>
	 * </ul>
	 * 
	 * @param target
	 *            瑕佽浆鎹㈡垚 {@code JSON} 鐨勭洰鏍囧璞°��
	 * @param excludesFieldsWithoutExpose
	 *            鏄惁鎺掗櫎鏈爣娉� {@literal @Expose} 娉ㄨВ鐨勫瓧娈点��
	 * @return 鐩爣瀵硅薄鐨� {@code JSON} 鏍煎紡鐨勫瓧绗︿覆銆�
	 */
	public static String toJson(Object target,
			boolean excludesFieldsWithoutExpose) {
		return toJson(target, null, false, null, null,
				excludesFieldsWithoutExpose);
	}

	/**
	 * 灏嗙粰瀹氱殑鐩爣瀵硅薄杞崲鎴� {@code JSON} 鏍煎紡鐨勫瓧绗︿覆銆�<strong>姝ゆ柟娉曞彧鐢ㄦ潵杞崲鏅�氱殑 {@code JavaBean}
	 * 瀵硅薄銆�</strong>
	 * <ul>
	 * <li>璇ユ柟娉曚笉浼氳浆鎹� {@code null} 鍊煎瓧娈碉紱</li>
	 * <li>璇ユ柟娉曡浆鎹㈡椂浣跨敤榛樿鐨� 鏃ユ湡/鏃堕棿 鏍煎紡鍖栨ā寮� - {@code yyyy-MM-dd HH:mm:ss SSS}锛�</li>
	 * </ul>
	 * 
	 * @param target
	 *            瑕佽浆鎹㈡垚 {@code JSON} 鐨勭洰鏍囧璞°��
	 * @param version
	 *            瀛楁鐨勭増鏈彿娉ㄨВ({@literal @Since})銆�
	 * @param excludesFieldsWithoutExpose
	 *            鏄惁鎺掗櫎鏈爣娉� {@literal @Expose} 娉ㄨВ鐨勫瓧娈点��
	 * @return 鐩爣瀵硅薄鐨� {@code JSON} 鏍煎紡鐨勫瓧绗︿覆銆�
	 */
	public static String toJson(Object target, Double version,
			boolean excludesFieldsWithoutExpose) {
		return toJson(target, null, false, version, null,
				excludesFieldsWithoutExpose);
	}

	/**
	 * 灏嗙粰瀹氱殑鐩爣瀵硅薄杞崲鎴� {@code JSON} 鏍煎紡鐨勫瓧绗︿覆銆�<strong>姝ゆ柟娉曢�氬父鐢ㄦ潵杞崲浣跨敤娉涘瀷鐨勫璞°��</strong>
	 * <ul>
	 * <li>璇ユ柟娉曞彧浼氳浆鎹㈡爣鏈� {@literal @Expose} 娉ㄨВ鐨勫瓧娈碉紱</li>
	 * <li>璇ユ柟娉曚笉浼氳浆鎹� {@code null} 鍊煎瓧娈碉紱</li>
	 * <li>璇ユ柟娉曚細杞崲鎵�鏈夋湭鏍囨敞鎴栧凡鏍囨敞 {@literal @Since} 鐨勫瓧娈碉紱</li>
	 * <li>璇ユ柟娉曡浆鎹㈡椂浣跨敤榛樿鐨� 鏃ユ湡/鏃堕棿 鏍煎紡鍖栨ā寮� - {@code yyyy-MM-dd HH:mm:ss SSSS}锛�</li>
	 * </ul>
	 * 
	 * @param target
	 *            瑕佽浆鎹㈡垚 {@code JSON} 鐨勭洰鏍囧璞°��
	 * @param targetType
	 *            鐩爣瀵硅薄鐨勭被鍨嬨��
	 * @return 鐩爣瀵硅薄鐨� {@code JSON} 鏍煎紡鐨勫瓧绗︿覆銆�
	 */
	public static String toJson(Object target, Type targetType) {
		return toJson(target, targetType, false, null, null, false);
		// Gson gson = new Gson();
		// return gson.toJson(target, targetType);
	}

	/**
	 * 灏嗙粰瀹氱殑鐩爣瀵硅薄杞崲鎴� {@code JSON} 鏍煎紡鐨勫瓧绗︿覆銆�<strong>姝ゆ柟娉曢�氬父鐢ㄦ潵杞崲浣跨敤娉涘瀷鐨勫璞°��</strong>
	 * <ul>
	 * <li>璇ユ柟娉曞彧浼氳浆鎹㈡爣鏈� {@literal @Expose} 娉ㄨВ鐨勫瓧娈碉紱</li>
	 * <li>璇ユ柟娉曚笉浼氳浆鎹� {@code null} 鍊煎瓧娈碉紱</li>
	 * <li>璇ユ柟娉曡浆鎹㈡椂浣跨敤榛樿鐨� 鏃ユ湡/鏃堕棿 鏍煎紡鍖栨ā寮� - {@code yyyy-MM-dd HH:mm:ss SSSS}锛�</li>
	 * </ul>
	 * 
	 * @param target
	 *            瑕佽浆鎹㈡垚 {@code JSON} 鐨勭洰鏍囧璞°��
	 * @param targetType
	 *            鐩爣瀵硅薄鐨勭被鍨嬨��
	 * @param version
	 *            瀛楁鐨勭増鏈彿娉ㄨВ({@literal @Since})銆�
	 * @return 鐩爣瀵硅薄鐨� {@code JSON} 鏍煎紡鐨勫瓧绗︿覆銆�
	 */
	public static String toJson(Object target, Type targetType, Double version) {
		return toJson(target, targetType, false, version, null, true);
	}

	/**
	 * 灏嗙粰瀹氱殑鐩爣瀵硅薄杞崲鎴� {@code JSON} 鏍煎紡鐨勫瓧绗︿覆銆�<strong>姝ゆ柟娉曢�氬父鐢ㄦ潵杞崲浣跨敤娉涘瀷鐨勫璞°��</strong>
	 * <ul>
	 * <li>璇ユ柟娉曚笉浼氳浆鎹� {@code null} 鍊煎瓧娈碉紱</li>
	 * <li>璇ユ柟娉曚細杞崲鎵�鏈夋湭鏍囨敞鎴栧凡鏍囨敞 {@literal @Since} 鐨勫瓧娈碉紱</li>
	 * <li>璇ユ柟娉曡浆鎹㈡椂浣跨敤榛樿鐨� 鏃ユ湡/鏃堕棿 鏍煎紡鍖栨ā寮� - {@code yyyy-MM-dd HH:mm:ss SSS}锛�</li>
	 * </ul>
	 * 
	 * @param target
	 *            瑕佽浆鎹㈡垚 {@code JSON} 鐨勭洰鏍囧璞°��
	 * @param targetType
	 *            鐩爣瀵硅薄鐨勭被鍨嬨��
	 * @param excludesFieldsWithoutExpose
	 *            鏄惁鎺掗櫎鏈爣娉� {@literal @Expose} 娉ㄨВ鐨勫瓧娈点��
	 * @return 鐩爣瀵硅薄鐨� {@code JSON} 鏍煎紡鐨勫瓧绗︿覆銆�
	 */
	public static String toJson(Object target, Type targetType,
			boolean excludesFieldsWithoutExpose) {
		return toJson(target, targetType, false, null, null,
				excludesFieldsWithoutExpose);
	}

	/**
	 * 灏嗙粰瀹氱殑鐩爣瀵硅薄杞崲鎴� {@code JSON} 鏍煎紡鐨勫瓧绗︿覆銆�<strong>姝ゆ柟娉曢�氬父鐢ㄦ潵杞崲浣跨敤娉涘瀷鐨勫璞°��</strong>
	 * <ul>
	 * <li>璇ユ柟娉曚笉浼氳浆鎹� {@code null} 鍊煎瓧娈碉紱</li>
	 * <li>璇ユ柟娉曡浆鎹㈡椂浣跨敤榛樿鐨� 鏃ユ湡/鏃堕棿 鏍煎紡鍖栨ā寮� - {@code yyyy-MM-dd HH:mm:ss SSS}锛�</li>
	 * </ul>
	 * 
	 * @param target
	 *            瑕佽浆鎹㈡垚 {@code JSON} 鐨勭洰鏍囧璞°��
	 * @param targetType
	 *            鐩爣瀵硅薄鐨勭被鍨嬨��
	 * @param version
	 *            瀛楁鐨勭増鏈彿娉ㄨВ({@literal @Since})銆�
	 * @param excludesFieldsWithoutExpose
	 *            鏄惁鎺掗櫎鏈爣娉� {@literal @Expose} 娉ㄨВ鐨勫瓧娈点��
	 * @return 鐩爣瀵硅薄鐨� {@code JSON} 鏍煎紡鐨勫瓧绗︿覆銆�
	 */
	public static String toJson(Object target, Type targetType, Double version,
			boolean excludesFieldsWithoutExpose) {
		return toJson(target, targetType, false, version, null,
				excludesFieldsWithoutExpose);
	}

	/**
	 * 灏嗙粰瀹氱殑 {@code JSON} 瀛楃涓茶浆鎹㈡垚鎸囧畾鐨勭被鍨嬪璞°��
	 * 
	 * @param <T>
	 *            瑕佽浆鎹㈢殑鐩爣绫诲瀷銆�
	 * @param json
	 *            缁欏畾鐨� {@code JSON} 瀛楃涓层��
	 * @param token
	 *            {@code com.google.gson.reflect.TypeToken} 鐨勭被鍨嬫寚绀虹被瀵硅薄銆�
	 * @param datePattern
	 *            鏃ユ湡鏍煎紡妯″紡銆�
	 * @return 缁欏畾鐨� {@code JSON} 瀛楃涓茶〃绀虹殑鎸囧畾鐨勭被鍨嬪璞°��
	 */
	public static <T> T fromJson(String json, TypeToken<T> token,
			String datePattern) {
		if (isEmpty(json)) {
			return null;
		}
		GsonBuilder builder = new GsonBuilder();
		if (isEmpty(datePattern)) {
			datePattern = DEFAULT_DATE_PATTERN;
		}
		Gson gson = builder.create();
		try {
			return gson.fromJson(json, token.getType());
		} catch (Exception ex) {
			Log.e(TAG, json + " 鏃犳硶杞崲涓� " + token.getRawType().getName() + " 瀵硅薄!");
			// log.error(json + " 鏃犳硶杞崲涓� " + token.getRawType().getName() +
			// " 瀵硅薄!",
			// ex);
			return null;
		}
	}

	/**
	 * 灏嗙粰瀹氱殑 {@code JSON} 瀛楃涓茶浆鎹㈡垚鎸囧畾鐨勭被鍨嬪璞°��
	 * 
	 * @param <T>
	 *            瑕佽浆鎹㈢殑鐩爣绫诲瀷銆�
	 * @param json
	 *            缁欏畾鐨� {@code JSON} 瀛楃涓层��
	 * @param token
	 *            {@code com.google.gson.reflect.TypeToken} 鐨勭被鍨嬫寚绀虹被瀵硅薄銆�
	 * @return 缁欏畾鐨� {@code JSON} 瀛楃涓茶〃绀虹殑鎸囧畾鐨勭被鍨嬪璞°��
	 */
	public static <T> T fromJson(String json, TypeToken<T> token) {
		return fromJson(json, token, null);
	}

	/**
	 * 灏嗙粰瀹氱殑 {@code JSON} 瀛楃涓茶浆鎹㈡垚鎸囧畾鐨勭被鍨嬪璞°��<strong>姝ゆ柟娉曢�氬父鐢ㄦ潵杞崲鏅�氱殑 {@code JavaBean}
	 * 瀵硅薄銆�</strong>
	 * 
	 * @param <T>
	 *            瑕佽浆鎹㈢殑鐩爣绫诲瀷銆�
	 * @param json
	 *            缁欏畾鐨� {@code JSON} 瀛楃涓层��
	 * @param clazz
	 *            瑕佽浆鎹㈢殑鐩爣绫汇��
	 * @param datePattern
	 *            鏃ユ湡鏍煎紡妯″紡銆�
	 * @return 缁欏畾鐨� {@code JSON} 瀛楃涓茶〃绀虹殑鎸囧畾鐨勭被鍨嬪璞°��
	 */
	public static <T> T fromJson(String json, Class<T> clazz, String datePattern) {
		if (isEmpty(json)) {
			return null;
		}
		GsonBuilder builder = new GsonBuilder();
		if (isEmpty(datePattern)) {
			datePattern = DEFAULT_DATE_PATTERN;
		}
		Gson gson = builder.create();
		try {
			return gson.fromJson(json, clazz);
		} catch (Exception ex) {
			Log.e(TAG, json + " 鏃犳硶杞崲涓� " + clazz.getName() + " 瀵硅薄!");
			// log.error(json + " 鏃犳硶杞崲涓� " + clazz.getName() + " 瀵硅薄!", ex);
			return null;
		}
	}

	/**
	 * 灏嗙粰瀹氱殑 {@code JSON} 瀛楃涓茶浆鎹㈡垚鎸囧畾鐨勭被鍨嬪璞°��<strong>姝ゆ柟娉曢�氬父鐢ㄦ潵杞崲鏅�氱殑 {@code JavaBean}
	 * 瀵硅薄銆�</strong>
	 * 
	 * @param <T>
	 *            瑕佽浆鎹㈢殑鐩爣绫诲瀷銆�
	 * @param json
	 *            缁欏畾鐨� {@code JSON} 瀛楃涓层��
	 * @param clazz
	 *            瑕佽浆鎹㈢殑鐩爣绫汇��
	 * @return 缁欏畾鐨� {@code JSON} 瀛楃涓茶〃绀虹殑鎸囧畾鐨勭被鍨嬪璞°��
	 */
	public static <T> T fromJson(String json, Class<T> clazz) {
		return fromJson(json, clazz, null);
	}

	public static boolean isEmpty(String inStr) {
		boolean reTag = false;
		if (inStr == null || "".equals(inStr)) {
			reTag = true;
		}
		return reTag;
	}
	
}
