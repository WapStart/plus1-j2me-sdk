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

package formApp;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

import plus1.*;

/**
 * @author Alexander Zaytsev <a.zaytsev@co.wapstart.ru>
 * @copyright Copyright (c) 2011, Wapstart
 */
final public class Plus1FormMidlet extends MIDlet
	implements Plus1AdListener, CommandListener {

	private Plus1BannerAsker asker = null;
	private Plus1BannerRequest request = null;
	private Form mainForm = null;

	public void startApp() {
		this.switchDisplayable(this.getForm());

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

	public void dispatchNewBanner(Plus1Banner banner) {
		banner.appendToForm(this.getForm(), true);
	}

	public void commandAction(Command command, Displayable d) {
		if (command.getCommandType() == Command.EXIT) {
			this.exitMIDlet();
		} else if (command.getClass().getName().equals("plus1.Plus1BannerCommand")) {
			Plus1BannerCommand plus1Command = (Plus1BannerCommand) command;

			try {
				platformRequest(plus1Command.getLink());
			} catch (Exception e) {}
		}
	}

	private Form getForm() {
		if (this.mainForm == null) {
			this.mainForm = new Form("Example form");
			this.mainForm.append("Some text in form\n");

			this.mainForm.setCommandListener(this);

			this.mainForm.addCommand(new Command("Выход", Command.EXIT, 0));
		}

		return this.mainForm;
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
}
