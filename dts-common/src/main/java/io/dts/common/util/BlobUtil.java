package io.dts.common.util;


import javax.sql.rowset.serial.SerialBlob;

import io.dts.common.exception.DtsException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;

public class BlobUtil {
	public static Blob string2blob(String str) {
		if (str == null) {
			return null;
		}

		try {
			return new SerialBlob(str.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
			throw (DtsException) e;
		}
	}

	public static String blob2string(Blob blob) {
		if (blob == null) {
			return null;
		}

		try {
			return new String(blob.getBytes((long) 1, (int) blob.length()));
		} catch (Exception e) {
			e.printStackTrace();
			throw (DtsException) e;
		}
	}

	public static String inputStream2String(InputStream is) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int i = -1;
			while ((i = is.read()) != -1) {
				baos.write(i);
			}
			return baos.toString();
		} catch (Exception e) {
			e.printStackTrace();
			throw (DtsException) e;
		}
	}

	// 测试方法
	public static void main(String[] args) throws IOException {
		// 测试字符串
		String str = "%5B%7B%22lastUpdateTime%22%3A%222011-10-28+9%3A39%3A41%22%2C%22smsList%22%3A%5B%7B%22liveState%22%3A%221";

		System.out.println("原长度：" + str.length());
		System.out.println("原长度：" + BlobUtil.string2blob(str));
	}
}
