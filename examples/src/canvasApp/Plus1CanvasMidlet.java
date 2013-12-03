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

package canvasApp;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.location.Criteria;

import plus1.*;

/**
 * @author Evgeny Kokovikhin <e.kokovikhin@co.wapstart.ru>
 * @copyright Copyright (c) 2010, Wapstart
 */
final public class Plus1CanvasMidlet extends MIDlet
	implements Plus1AdListener, Plus1ExceptionListener, CommandListener {

	private Plus1LocationDetector detector = null;
	private Plus1BannerAsker asker = null;
	private Plus1BannerRequest request = null;
	private Plus1TestCanvas canvas = null;

	public void startApp() {
		this.getCanvas().addString("This is debug console in test app");
		this.switchDisplayable(this.getCanvas());

		this.getLocationDetector().start();
		// NOTE: we use custom location detector
		this.getAsker().disableAutoDetectLocation(true);

		this.getAsker().init();
		this.getCanvas().addString("Success init of asker");
	}

	public void pauseApp() {
		this.getCanvas().addString("Pause app");
		this.getCanvas().repaint();
		notifyPaused();
	}

	public void destroyApp(boolean unconditional) {
		notifyDestroyed();
	}

	public void exitMIDlet() {
		this.switchDisplayable(null);
		destroyApp(true);
	}

	public void switchDisplayable(Displayable nextDisplayable) {
		this.getDisplay().setCurrent(nextDisplayable);
	}

	public Display getDisplay() {
		return Display.getDisplay(this);
	}

	public void dispatchNewBanner(Plus1Banner banner) {
		if (this.getCanvas().hasBanner() && this.getCanvas().getBanner().isAnimatedGif())
			this.getCanvas().getBanner().destroyGifPlayer();

		int width = this.getCanvas().getWidth();
		int height = this.getCanvas().getHeight();

		banner.setDisplayWidth(width);
		banner.setDisplayLocation(
			0,
			height - banner.getBannerImageHeight(width)
		);

		this.getCanvas().setBanner(banner);
		this.getCanvas().repaint();
		if (banner.isAnimatedGif())
			banner.attachGifPlayer(this.getCanvas());
	}

	public void handleException(Exception e) {
		this.getCanvas().addString("Error class: " + e.getClass().toString());
		this.getCanvas().addString("Error message: " + e.getMessage());
		this.getCanvas().repaint();
	}

	public void commandAction(Command command, Displayable d) {
		if (command.getCommandType() == Command.EXIT) {
			if (this.getCanvas().hasBanner()) {
				if (this.getCanvas().getBanner().isAnimatedGif())
					this.getCanvas().getBanner().destroyGifPlayer();
				this.getCanvas().dropBanner();
				this.getCanvas().repaint();
			} else
				this.exitMIDlet();
		} else if (command.getClass().getName().equals("plus1.Plus1BannerCommand")) {
			Plus1BannerCommand plus1Command = (Plus1BannerCommand) command;

			try {
				platformRequest(plus1Command.getLink());
			} catch (Exception e) {}
		}
	}

	private Plus1LocationDetector getLocationDetector() {
		if (this.detector == null) {
			this.detector = new Plus1LocationDetector(this.getRequest());
			this.detector.setListener(this);
			this.detector.setCriteria(this.makeCriteria());
			this.detector.setInterval(120); // seconds
			this.detector.setTimeout(100); // seconds
		}

		return this.detector;
	}

	private Criteria makeCriteria() {
		Criteria cr = new Criteria();
		cr.setHorizontalAccuracy(1000);
		cr.setSpeedAndCourseRequired(false);
		cr.setAltitudeRequired(false);
		cr.setPreferredPowerConsumption(Criteria.NO_REQUIREMENT);

		return cr;
	}

	private Plus1BannerAsker getAsker() {
		if (this.asker == null) {
			this.asker = new Plus1BannerAsker(this.getRequest());
			this.asker.setListener(this);
			this.asker.setRequestInterval(7000); // milliseconds
			//this.asker.setTimeout(3000); // milliseconds
		}

		return this.asker;
	}

	private Plus1BannerRequest getRequest() {
		if (this.request == null) {
			this.request = Plus1BannerRequest.create()
				.setLogin(Plus1Helper.sha1("SomeUserLogin"))
				//.setAge(22)
				//.setGender(Plus1Gender.MALE)
				.setApplicationId(/* PLACE YOUR APPLICATION ID HERE */);
		}

		return this.request;
	}

	private Plus1TestCanvas getCanvas() {
		if (this.canvas == null)
			this.canvas = new Plus1TestCanvas(this);

		return this.canvas;
	}
}
