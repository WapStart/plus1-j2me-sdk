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

import java.util.Vector;
import java.io.IOException;
import javax.microedition.location.Location;
import javax.microedition.location.Coordinates;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

/**
 * @author Evgeny Kokovikhin <e.kokovikhin@co.wapstart.ru>
 * @copyright Copyright (c) 2010, Wapstart
 */
public class Plus1BannerRequest extends Object {

	private static final int VERSION = 2;

	private static String pageId = null;
	private static Location location = null;
	private static String userAgent = null;
	private static String displayMetrics = null;
	private static String clientSessionId = null;
	private static String preferredLocale = null;
	private static boolean userAgentFetched = false;
	private static boolean preferredLocaleFetched = false;
	private static boolean imeiFetched = false;
	private static String imei = null;
	private String rotatorUri = "http://ro.plus1.wapstart.ru/";
	private Plus1Encoding encoding = Plus1Encoding.UTF8;
	private int applicationId = -1;
	private Plus1Gender gender = null;
	private int age = -1;
	private Vector typesList = new Vector();
	private String login = null;
	private boolean disableDispatchIMEI = false;

	public static Plus1BannerRequest create() {
		return new Plus1BannerRequest();
	}

	public Plus1BannerRequest() { /*_*/ }

	// FIXME: add rotator URL check
	public Plus1BannerRequest setBaseRotatorUri(String rotatorUri)
	{
		this.rotatorUri = rotatorUri;

		return this;
	}

	public String getBaseRotatorUri()
	{
		return this.rotatorUri;
	}

	public Plus1BannerRequest setApplicationId(int applicationId) {
		if (applicationId < 1)
			throw new IllegalArgumentException("Wrong applicationId argument");

		this.applicationId = applicationId;

		return this;
	}

	public int getApplicationId() {
		return this.applicationId;
	}

	public Plus1BannerRequest setGender(Plus1Gender gender) {
		this.gender = gender;

		return this;
	}

	public Plus1Gender getGender() {
		return gender;
	}

	public Plus1BannerRequest setAge(int age) {
		if (age < 1)
			throw new IllegalArgumentException("Wrong age argument");

		this.age = age;

		return this;
	}

	public int getAge() {
		return age;
	}

	public Plus1BannerRequest setEncoding(Plus1Encoding encoding) {
		this.encoding = encoding;

		return this;
	}

	public Plus1Encoding getEncoding() {
		return this.encoding;
	}

	public Plus1BannerRequest setLogin(String login) {
		this.login = login;

		return this;
	}

	public String getLogin() {
		return this.login;
	}

	public Plus1BannerRequest setLocation(Location lc) {
		location = lc;

		return this;
	}

	public Location getLocation() {
		return location;
	}

	public Plus1BannerRequest disableDispatchIMEI(boolean disable) {
		this.disableDispatchIMEI = disable;

		return this;
	}

	public boolean isDisabledIMEIDispatch() {
		return this.disableDispatchIMEI;
	}

	public String getPageId() {
		if (pageId == null)
			pageId = Plus1Helper.makeUniqueHash();

		return pageId;
	}

	public void regeneratePageId() {
		pageId = null;
	}

	public String toString() {

		String url = this.getBaseRotatorUri()
			+ "?area=application"
			+ "&version=" + VERSION
			+ "&id=" + this.getApplicationId()
			+ "&pageId=" + this.getPageId()
			+ "&encoding=" + this.getEncoding().getId();

		if (this.getGender() != null)
			url += "&sex=" + this.getGender().getId();

		if (this.getAge() > 0)
			url += "&age=" + this.getAge();

		int typesListSize = this.getTypesList().size();
		if (typesListSize > 0) {
			for (int i = 0; i < typesListSize; i++)
				url += "&types[]=" + ((Plus1Type) this.getTypesList().elementAt(i)).getId();
		}

		if (this.getLogin() != null)
			url += "&login=" + Plus1Helper.encodeUrl(this.getLogin());

		if (this.getLocation() != null) {
			Coordinates c = this.getLocation().getQualifiedCoordinates();

			if (c != null)
				url += "&location=" + c.getLatitude() + ";" + c.getLongitude();
		}

		return url;
	}

	public HttpConnection toHttpConnection() throws IOException {
		HttpConnection hc = (HttpConnection) Connector.open(this.toString());
		hc.setRequestMethod(HttpConnection.GET);
		this.modifyConnection(hc);

		return hc;
	}

	private void modifyConnection(HttpConnection connection) throws IOException {
		if (this.getUserAgent() != null)
			connection.setRequestProperty(
				"User-Agent",
				this.getUserAgent()
			);

		connection.setRequestProperty(
			"Cookies",
			"wssid=" + this.getClientSessionId()
		);
		connection.setRequestProperty(
			"x-display-metrics",
			this.getDisplayMetrics()
		);
		connection.setRequestProperty(
			"x-application-type",
			"Java ME"
		);

		if (this.getPreferredLocale() != null)
			connection.setRequestProperty(
				"x-preferred-locale",
				this.getPreferredLocale()
			);

		if (!this.isDisabledIMEIDispatch() && this.getIMEI() != null)
			connection.setRequestProperty(
				"x-device-imei",
				this.getIMEI()
			);
	}

	// NOTE: unimplemented now
	private Plus1BannerRequest addType(Plus1Type type) {
		this.typesList.addElement(type);

		return this;
	}

	private Vector getTypesList() {
		return this.typesList;
	}

	private String getUserAgent() {
		if (!userAgentFetched) {
			userAgent = Plus1Helper.detectUserAgent();
			userAgentFetched = true;
		}

		return userAgent;
	}

	private String getDisplayMetrics() {
		if (displayMetrics == null)
			displayMetrics = Plus1Helper.detectDisplayMetrics();

		return displayMetrics;
	}

	private String getPreferredLocale() {
		if (!preferredLocaleFetched) {
			preferredLocale = Plus1Helper.detectLocale();
			preferredLocaleFetched = true;
		}

		return preferredLocale;
	}

	private String getIMEI() {
		if (!imeiFetched) {
			imei = Plus1Helper.detectIMEI();
			imeiFetched = true;
		}

		return imei;
	}

	private String getClientSessionId() {
		if (clientSessionId == null) {
			// NOTE: base on IMEI if exists, otherwise use RecordStore
			clientSessionId =
				this.getIMEI() != null
					? Plus1Helper.sha1(this.getIMEI())
					: Plus1Helper.getClientSessionFromStorage();

			if (clientSessionId == null) {
				clientSessionId = Plus1Helper.makeUniqueHash();

				Plus1Helper.saveClientSessionToStorage(clientSessionId);
			}
		}

		return clientSessionId;
	}
}
