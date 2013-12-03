/**
 * Copyright (c) 2011, Alexander Zaytsev <a.zaytsev@co.wapstart.ru>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   * Neither the name of the "Wapstart" nor the names
 *     of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package plus1;

import java.util.Random;
import java.security.MessageDigest;

import javax.microedition.media.Manager;
import javax.microedition.rms.*;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Graphics;

/**
 * @author Alexander Zaytsev <a.zaytsev@co.wapstart.ru>
 * @copyright Copyright (c) 2011, Wapstart
 */
public class Plus1Helper {

	private static final String STORAGE_NAME = "plus1SessionStorage";
	private static final int STORAGE_ID = 1;

	private Plus1Helper() { /*_*/ }

	public static String makeUniqueHash() {
		Random rnd = new Random();
		byte[] data = new byte[20];
		for (int i=0; i<20; i++)
			data[i] = (byte) rnd.nextInt();

		return sha1(data);
	}

	public static String sha1(String text) {
		return sha1(text.getBytes());
	}

	public static String sha1(byte[] data) {
		byte[] sha1hash = new byte[20]; // 160 bits for digest

		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");

			md.update(data, 0, data.length);
			md.digest(sha1hash, 0, sha1hash.length);
		} catch (Exception e) {
			throw new IllegalStateException(
				"Creating hash error: " + e.getMessage()
			);
		}

		return convertToHex(sha1hash);
	}

	public static String detectUserAgent() {
		return System.getProperty("microedition.platform");
	}

	public static String detectDisplayMetrics() {
		Plus1HelperCanvas canvas = new Plus1HelperCanvas();

		return canvas.getWidth() + "x" + canvas.getHeight();
	}

	public static String detectLocale() {
		return System.getProperty("microedition.locale");
	}

	public static String detectIMEI() {
		String[] propertyList = {
			// Nokia
			"phone.imei",
			"com.nokia.imei",
			"com.nokia.mid.imei",
			// Sony Ericsson
			"com.sonyericsson.imei",
			// Samsung
			"com.samsung.imei",
			// Motorola
			"IMEI",
			"com.motorola.IMEI",
			// Siemens
			"com.siemens.IMEI",
			// LG
			"com.lge.imei"
		};

		String imei;
		for (int i=0; i<propertyList.length; i++) {
			imei = System.getProperty(propertyList[i]);

			if (imei != null)
				return imei;
		}

		return null;
	}

	public static boolean hasNativeGifDecode() {
		String[] list = Manager.getSupportedContentTypes(null);
		for (int i=0; i<list.length; i++)
			if (list[i].equals("image/gif"))
				return true;

		return false;
	}

	public static String encodeUrl(String s) {
		StringBuffer sbuf = new StringBuffer();
		int ch;
		for (int i = 0; i < s.length(); i++) {
			ch = s.charAt(i);
			switch (ch) {
				case ' ':	sbuf.append("+");	break;
				case '!':	sbuf.append("%21");	break;
				case '*':	sbuf.append("%2A");	break;
				case '\'':	sbuf.append("%27");	break;
				case '(':	sbuf.append("%28");	break;
				case ')':	sbuf.append("%29");	break;
				case ';':	sbuf.append("%3B");	break;
				case ':':	sbuf.append("%3A");	break;
				case '@':	sbuf.append("%40");	break;
				case '&':	sbuf.append("%26");	break;
				case '=':	sbuf.append("%3D");	break;
				case '+':	sbuf.append("%2B");	break;
				case '$':	sbuf.append("%24");	break;
				case ',':	sbuf.append("%2C");	break;
				case '/':	sbuf.append("%2F");	break;
				case '?':	sbuf.append("%3F");	break;
				case '%':	sbuf.append("%25");	break;
				case '#':	sbuf.append("%23");	break;
				case '[':	sbuf.append("%5B");	break;
				case ']':	sbuf.append("%5D");	break;
				default:	sbuf.append((char) ch);
			}
		}

		return sbuf.toString();
	}

	public static String decodeUrl(String url) {
		String startString;
		String endString;

		while (url.indexOf("&amp;") != -1) {
			startString = url.substring(0, url.indexOf("&amp;")) + "&";
			endString = url.substring(url.indexOf("&amp;") + 5);
			url = startString + endString;
		}

		return url;
	}

	public static String getClientSessionFromStorage() {
		String sessionId;

		try {
			RecordStore rs = RecordStore.openRecordStore(STORAGE_NAME, true);

			byte[] record = rs.getRecord(STORAGE_ID);
			sessionId = new String(record, 0, record.length);

			rs.closeRecordStore();

		} catch (Exception e) {
			sessionId = null;
		}

		return sessionId;
	}

	public static void saveClientSessionToStorage(String sessionId) {
		try {
			RecordStore rs = RecordStore.openRecordStore(STORAGE_NAME, true);

			rs.setRecord(STORAGE_ID, sessionId.getBytes(), 0, sessionId.length());

			rs.closeRecordStore();

		} catch (Exception e) { /*_*/ }
	}

	public static Image resizeImage(Image src, int width, int height) {
		int srcWidth = src.getWidth();
		int srcHeight = src.getHeight();

		Image tmp = Image.createImage(width, srcHeight);
		Graphics g = tmp.getGraphics();
		int ratio = (srcWidth << 16) / width;
		int pos = ratio/2;

		// Horizontal Resize
		for (int x = 0; x < width; x++) {
			g.setClip(x, 0, 1, srcHeight);
			g.drawImage(src, x - (pos >> 16), 0, Graphics.LEFT | Graphics.TOP);
			pos += ratio;
		}

		Image resizedImage = Image.createImage(width, height);
		g = resizedImage.getGraphics();
		ratio = (srcHeight << 16) / height;
		pos = ratio/2;

		// Vertical resize
		for (int y = 0; y < height; y++) {
			g.setClip(0, y, width, 1);
			g.drawImage(tmp, 0, y - (pos >> 16), Graphics.LEFT | Graphics.TOP);
			pos += ratio;
		}

		return resizedImage;
	}

	private static String convertToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9)) {
					buf.append((char) ('0' + halfbyte));
				} else {
					buf.append((char) ('a' + (halfbyte - 10)));
				}
				halfbyte = data[i] & 0x0F;
			} while (two_halfs++ < 1);
		}

		return buf.toString();
	}
}
