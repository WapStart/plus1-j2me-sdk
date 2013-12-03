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

import javax.microedition.location.*;

/**
 * @author Alexander Zaytsev <a.zaytsev@co.wapstart.ru>
 * @copyright Copyright (c) 2011, Wapstart
 */
public class Plus1LocationDetector implements LocationListener {

	private Plus1BannerRequest request;
	private LocationProvider lp;
	private Criteria criteria = null;
	private int interval = -1; // seconds
	private int timeout = -1; // seconds
	private int age = -1;
	private Plus1ExceptionListener listener = null;

	static public Plus1LocationDetector create(Plus1BannerRequest request) {
		return new Plus1LocationDetector(request);
	}

	public Plus1LocationDetector(Plus1BannerRequest request) {
		this.request = request;
	}

	public void locationUpdated(LocationProvider lp, Location lctn) {
		this.request.setLocation(lctn);
	}

	public void providerStateChanged(LocationProvider lp, int i) {
		if (i != LocationProvider.AVAILABLE)
			this.request.setLocation(null);
	}

	public Plus1LocationDetector setCriteria(Criteria cr) {
		this.criteria = cr;

		return this;
	}

	public Criteria getCriteria() {
		if (this.criteria == null)
			this.criteria = this.makeDefaultCriteria();

		return this.criteria;
	}

	public Plus1LocationDetector setInterval(int interval) {
		if (interval < 1)
			throw new IllegalArgumentException("Wrong interval argument");

		this.interval = interval;

		return this;
	}

	public int getInterval() {
		return this.interval;
	}

	public Plus1LocationDetector dropInterval() {
		this.interval = -1;

		return this;
	}

	public Plus1LocationDetector setTimeout(int timeout) {
		if (timeout < 1)
			throw new IllegalArgumentException("Wrong timeout argument");

		this.timeout = timeout;

		return this;
	}

	public int getTimeout() {
		return this.timeout;
	}

	public Plus1LocationDetector dropTimeout() {
		this.timeout = -1;

		return this;
	}

	public Plus1LocationDetector setAge(int age) {
		if (age < 1)
			throw new IllegalArgumentException("Wrong age argument");

		this.age = age;

		return this;
	}

	public int getAge() {
		return this.age;
	}

	public Plus1LocationDetector dropAge() {
		this.age = -1;

		return this;
	}

	public int getProviderState() {
		return
			this.lp == null
				? LocationProvider.OUT_OF_SERVICE
				: this.lp.getState();
	}

	public Plus1LocationDetector setListener(Plus1ExceptionListener listener) {
		this.listener = listener;

		return this;
	}

	public void start() {
		try {
			this.lp = LocationProvider.getInstance(this.getCriteria());

			this.request.setLocation(LocationProvider.getLastKnownLocation());

			this.lp.setLocationListener(
				this,
				this.getInterval(),
				this.getTimeout(),
				this.getAge()
			);
		} catch (Exception e) {
			if (this.listener != null)
				this.listener.handleException(e);
		}
	}

	public void stop() {
		if (this.lp != null) {
			this.lp.setLocationListener(null, -1, -1, -1);
			this.lp.reset();
			this.lp = null;
		}
	}

	private Criteria makeDefaultCriteria() {
		Criteria cr = new Criteria();
		cr.setHorizontalAccuracy(500);
		cr.setSpeedAndCourseRequired(false);
		cr.setAltitudeRequired(false);
		cr.setPreferredPowerConsumption(Criteria.NO_REQUIREMENT);

		return cr;
	}
}
