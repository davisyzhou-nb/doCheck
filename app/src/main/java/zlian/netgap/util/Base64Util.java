package zlian.netgap.util;

import android.util.Base64;

import java.lang.reflect.Method;


public class Base64Util {
	/***
	 * encode by Base64
	 */
	public static String encodeBase64(byte[]input) throws Exception{
		Class clazz=Class.forName("com.sun.org.apache.xerces.internal.impl.dv.util.Base64");
		Method mainMethod= clazz.getMethod("encode", byte[].class);
		mainMethod.setAccessible(true);
		Object retObj=mainMethod.invoke(null, new Object[]{input});
		return (String)retObj;
	}
	/***
	 * decode by Base64
	 */
	public static byte[] decodeBase64(String input) throws Exception{
		Class clazz=Class.forName("com.sun.org.apache.xerces.internal.impl.dv.util.Base64");
		Method mainMethod= clazz.getMethod("decode", String.class);
		mainMethod.setAccessible(true);
		Object retObj=mainMethod.invoke(null, input);
		return (byte[])retObj;
	}

	/**
	 * @param bytes
	 * @return
	 */
	public static byte[] decode(final byte[] bytes) {
		return Base64.decode(bytes, Base64.DEFAULT);
	}

	/**
	 * 二进制数据编码为BASE64字符串
	 *
	 * @param bytes
	 * @return
	 * @throws Exception
	 */
	public static String encode(final byte[] bytes) {
		return new String(Base64.encode(bytes,Base64.DEFAULT));
	}

}
