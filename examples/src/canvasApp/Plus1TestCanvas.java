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

import java.util.Random;
import java.util.Vector;
import java.io.*;
import javax.microedition.lcdui.*;
import plus1.*;

/**
 * @author Evgeny Kokovikhin <e.kokovikhin@co.wapstart.ru>
 * @copyright Copyright (c) 2010, Wapstart
 */
public class Plus1TestCanvas extends Canvas {

	private CommandListener listener = null;
	private Plus1Banner banner = null;
	private Plus1BannerCommand bannerCommand = null;
	private Image bart = null;
	private Vector stringList = new Vector();

	public Plus1TestCanvas(CommandListener listener) {
		this.listener = listener;

		this.addCommand(new Command("Выход", Command.EXIT, 0));
		this.setCommandListener(this.listener);
	}

	public void setBanner(Plus1Banner banner) {
		this.banner = banner;

		if (this.bannerCommand != null) {
			this.removeCommand(this.bannerCommand);
		}
		this.bannerCommand = this.banner.getCommand("Перейти", "Перейти по ссылке", 1);
		this.addCommand(this.bannerCommand);
	}

	public Plus1Banner getBanner() {
		return this.banner;
	}

	public void dropBanner() {
		this.banner = null;

		if (this.bannerCommand != null)
			this.removeCommand(this.bannerCommand);
	}

	public boolean hasBanner() {
		return (this.banner != null);
	}

	public void addString(String str) {
		this.stringList.addElement(str);
	}

	public Vector getStringList() {
		return this.stringList;
	}

	protected void paint(Graphics g) {
		Random r = new Random();
		g.setColor(r.nextInt(255), r.nextInt(255), r.nextInt(255));

		g.fillRect(0, 0, this.getWidth(), this.getHeight());

		try {
			g.drawImage(this.getBartImage(), 0, 0, 0);
		} catch (IOException e) {}

		int count = this.getStringList().size();
		if (count > 0) {
			g.setColor(0, 0, 0);
			g.setFont(Font.getDefaultFont());
			for (int i=0; i<count; i++) {
				String str = (String)this.getStringList().elementAt(i);
				g.drawString(str, 10, i*22+10, Graphics.LEFT | Graphics.TOP);
			}
		}

		if (this.hasBanner())
			this.drawBanner(g);
	}

	private void drawBanner(Graphics g) {
		this.getBanner().render(g);
	}

	private Image getBartImage() throws IOException
	{
		if (this.bart == null) {
			this.bart = Image.createImage("/BartSimpson.png");
		}

		return this.bart;
	}
}
