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

package layersApp;

import plus1.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.LayerManager;
import javax.microedition.lcdui.game.Sprite;

/**
 * @author Evgeny Kokovikhin <e.kokovikhin@co.wapstart.ru>
 * @copyright Copyright (c) 2010, Wapstart
 */
public class Plus1LayersMidlet extends MIDlet
	implements Plus1AdListener, Plus1ExceptionListener, CommandListener {

	private Plus1BannerAsker asker;
	private Plus1BannerRequest request;
	private LayerManager manager = null;
	private Plus1TestCanvas canvas = null;
	private Plus1Banner currentBanner = null;
	private Sprite bannerSprite = null;
	private Command bannerCommand = null;
	private Form mainForm = null;

	public void startApp() {
		this.switchDisplayable(this.getCanvas());

		this.getAsker().init();
	}

	public void pauseApp() {
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

	public LayerManager getManager() {
		if (this.manager == null) {

			this.manager = new LayerManager();

			try {
				Image bart = Image.createImage("/BartSimpson.png");
				Sprite bartSprite = new Sprite(bart);
				bartSprite.setVisible(true);

				this.manager.append(bartSprite);
			} catch (Exception e) { /*_*/ }
		}

		return this.manager;
	}

	public Form getForm() {
		if (this.mainForm == null) {
			this.mainForm = new Form("bash.org.ru");
			this.mainForm.append("Мама сегодня рассказала, что её подруге сегодня позвонили с детсада и сказали, что место освободилось, ведите ребенка. \nЭтот ребенок уже в 3(!) классе!\n");

			this.mainForm.setCommandListener(this);

			this.mainForm.addCommand(new Command("Выход", Command.EXIT, 0));
		}

		return this.mainForm;
	}

	public void handleException(Exception e) {
		this.getForm().append("Error class: " + e.getClass().toString());
		this.getForm().append("Error message: " + e.getMessage());

		this.getDisplay().setCurrent(this.getForm());
	}

	public void dispatchNewBanner(Plus1Banner banner) {
		if (this.currentBanner != null && this.currentBanner.isAnimatedGif())
			this.currentBanner.destroyGifPlayer();

		try {
			this.getCanvas().removeCommand(this.bannerCommand);
		} catch (Exception e) { }

		this.currentBanner = banner;
		this.bannerCommand = banner.getCommand("Перейти", "Перейти по ссылке", 1);

		int width = this.getCanvas().getWidth();
		int height = this.getCanvas().getHeight();

		banner.setDisplayWidth(width);
		banner.setDisplayLocation(
			0,
			height - banner.getBannerImageHeight(width)
		);

		try {
			this.getManager().remove(this.bannerSprite);
		} catch (Exception e) { }

		this.bannerSprite = banner.toSprite();
		this.getManager().insert(this.bannerSprite, 0);

		this.getCanvas().addCommand(this.bannerCommand);
		this.getCanvas().repaint();

		if (banner.isAnimatedGif())
			banner.attachGifPlayer(this.getCanvas());
	}

	public void commandAction(Command command, Displayable displayable) {
		if (command.getCommandType() == Command.EXIT) {
			if (this.currentBanner == null) {
				this.exitMIDlet();
			} else {
				if (this.currentBanner.isAnimatedGif())
					this.currentBanner.destroyGifPlayer();

				this.manager.remove(this.bannerSprite);
				this.getCanvas().repaint();
				this.getCanvas().removeCommand(this.bannerCommand);
				this.currentBanner = null;
			}
		} else if (command.getClass().getName().equals("plus1.Plus1BannerCommand")) {
			Plus1BannerCommand plus1Command = (Plus1BannerCommand) command;

			try {
				platformRequest(plus1Command.getLink());
			} catch (Exception e) { /*_*/ }
		}
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
		if (this.canvas == null) {
			this.canvas = new Plus1TestCanvas(this.getManager());

			this.canvas.setCommandListener(this);
			this.canvas.addCommand(new Command("Выход", Command.EXIT, 0));
		}

		return this.canvas;
	}
}
