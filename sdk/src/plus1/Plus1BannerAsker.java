/**
 * Copyright (c) 2010, Evgeniy Kokovikhin <e.kokovikhin@co.wapstart.ru>
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

import java.io.*;
import java.util.*;
import javax.microedition.io.*;
import org.kxml2.io.*;
import org.xmlpull.v1.*;

/**
 * @author Evgeny Kokovikhin <e.kokovikhin@co.wapstart.ru>
 * @copyright Copyright (c) 2010, Wapstart
 */
final public class Plus1BannerAsker extends Object {

	private final static String PLUS1_STUB = "<!-- i4jgij4pfd4ssd -->";
	private Plus1BannerRequest request;
	private int requestInterval = 0; // milliseconds
	private int timeout = 5000; // milliseconds
	private Plus1AdListener listener = null;
	private static Timer timer;
	private Plus1TimerTask timerTask;
	private boolean disableAutoDetectLocation = false;

	static public Plus1BannerAsker create(Plus1BannerRequest request) {
		return new Plus1BannerAsker(request);
	}

	public Plus1BannerAsker(Plus1BannerRequest request) {
		this.request = request;
	}

	public Plus1BannerAsker init() {
		if (this.listener == null)
			throw new IllegalStateException("Listener is not defined");
		if (this.request.getApplicationId() < 1)
			throw new IllegalStateException("Set application id before init");

		if (!this.isDisabledAutoDetectLocation()) {
			Plus1LocationDetector ld = new Plus1LocationDetector(this.request);
			ld.start();
		}

		if (this.requestInterval > 0) {
			timer = new Timer();
			this.timerTask = new Plus1TimerTask(this);

			timer.schedule(this.timerTask, 0, this.getRequestInterval());
		}

		return this;
	}

	public Plus1BannerAsker setRequestInterval(int requestInterval) {
		if (requestInterval < 1000)
			throw new IllegalArgumentException("Wrong interval argument");

		this.requestInterval = requestInterval;

		return this;
	}

	public int getRequestInterval() {
		return this.requestInterval;
	}

	public Plus1BannerAsker setTimeout(int timeout) {
		if (timeout < 1)
			throw new IllegalArgumentException("Wrong timeout argument");

		this.timeout = timeout;

		return this;
	}

	public int getTimeout() {
		return this.timeout;
	}

	public Plus1BannerAsker disableAutoDetectLocation(boolean disable) {
		this.disableAutoDetectLocation = disable;

		return this;
	}

	public boolean isDisabledAutoDetectLocation() {
		return this.disableAutoDetectLocation;
	}

	public void setListener(Plus1AdListener listener) {
		this.listener = listener;
	}

	// NOTE: use this in thread context
	public void fetchBanner() {
		try {
			String response = this.getRotatorResponse(this.request);

			if (!response.trim().equals(PLUS1_STUB)) {
				Plus1Banner banner = this.makeBanner(response);

				this.listener.dispatchNewBanner(banner);
			}
		} catch (Exception e) {
			if (this.listener instanceof Plus1ExceptionListener)
				((Plus1ExceptionListener)this.listener).handleException(e);
		}
	}

	private String getRotatorResponse(Plus1BannerRequest request)
		throws Plus1ResponseException, IOException
	{
		String result;
		InputStream is = null;
		HttpConnection hc = null;
		Timer timeoutTimer = new Timer();

		try {
			hc = request.toHttpConnection();

			timeoutTimer.schedule(new Plus1TimeoutTimerTask(hc), this.getTimeout());

			int respCode = hc.getResponseCode();

			if (respCode != hc.HTTP_OK)
				throw new Plus1ResponseException("Response code is: " + respCode);

			ByteArrayOutputStream bs = new ByteArrayOutputStream();
			is = hc.openDataInputStream();

			int chr;
			while ((chr = is.read()) != -1)
				bs.write(chr);

			result = new String(
				bs.toByteArray(),
				this.request.getEncoding().getName()
			);

		} finally {
			timeoutTimer.cancel();

			if (is != null)
				is.close();
			if (hc != null)
				hc.close();
		}

		return result;
	}

	private Plus1Banner makeBanner(String response) throws Plus1BannerException {
		Plus1Banner banner = new Plus1Banner();

		try {
			this.parseXMLResponse(response, banner);
		} catch (Exception e) {
			throw new Plus1BannerException("XML parse error");
		}

		return banner;
	}

	private void parseXMLResponse(String response, Plus1Banner banner)
		throws IOException, XmlPullParserException
	{
		ByteArrayInputStream is = new ByteArrayInputStream(
			response.getBytes(this.request.getEncoding().getName())
		);
		Reader reader = new InputStreamReader(is, this.request.getEncoding().getName());
		KXmlParser parser = new KXmlParser();
		parser.setInput(reader);

		parser.nextTag();
		parser.require(XmlPullParser.START_TAG, null, "banner");

		while (parser.nextTag () != XmlPullParser.END_TAG) {
			String propertyName = parser.getName();
			String value = parser.nextText();

			if (propertyName.equals("id")) {
				banner.setId(Integer.parseInt(value));
			} else if (propertyName.equals("title")) {
				banner.setTitle(value);
			} else if (propertyName.equals("content")) {
				banner.setContent(value);
			} else if (propertyName.equals("link")) {
				banner.setLink(value);
			} else if (propertyName.equals("pictureUrl")) {
				banner.setImagePath(value);
			} else if (propertyName.equals("pictureUrlPng")) {
				banner.setImagePngPath(value);
			}
		}

		parser.require(XmlPullParser.END_TAG, null, "banner");

		parser.next();
		parser.require(XmlPullParser.END_DOCUMENT, null, null);
	}
}
