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

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.Sprite;
import javax.microedition.io.*;
import java.io.*;
import java.util.Vector;

import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.control.GUIControl;
import javax.microedition.media.control.VideoControl;

/**
 * @author Evgeny Kokovikhin <e.kokovikhin@co.wapstart.ru>
 * @copyright Copyright (c) 2010, Wapstart
 */
public class Plus1Banner extends Object {

	private Player player = null;
	private int id;
	private String title;
	private String content;
	private String imagePath;
	private String imagePngPath;
	private String link;
	private Image image = null;
	private CommandListener listener = null;
	private int displayWidth = 0;
	private int displayX = 0;
	private int displayY = 0;
	private int relativeX = 0;
	private int relativeY = 0;

	public static Plus1Banner create() {
		return new Plus1Banner();
	}

	public Plus1Banner() { /*_*/ }

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return this.id;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return this.title;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getContent() {
		return this.content;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public String getImagePath() {
		return this.imagePath;
	}

	public void setImagePngPath(String imagePngPath) {
		this.imagePngPath = imagePngPath;
	}

	public String getImagePngPath() {
		return this.imagePngPath;
	}

	public void setLink(String link) {
		this.link = Plus1Helper.decodeUrl(link);
	}

	public String getLink() {
		return this.link;
	}

	public Image getImagePng() {
		if (
			(this.image == null)
			&& (this.getImagePngPath() != null)
			&& (!this.getImagePngPath().equals(""))
		) {
			try {
				this.image = this.loadImage(this.getImagePngPath());
			} catch (IOException e) { /*_*/ }
		}

		return this.image;
	}

	public Image getShield() throws IOException {
		return Image.createImage("/shild.png");
	}

	public void setListener(CommandListener listener) {
		this.listener = listener;
	}

	public CommandListener getListener() {
		return this.listener;
	}

	public Sprite toSprite() {
		Sprite sprite = new Sprite(this.toImage(this.getDisplayWidth()));
		sprite.setPosition(this.getDisplayX(), this.getDisplayY());

		return sprite;
	}

	public Image toImage(int width) {
		return this.toImage(width, this.getBannerImageHeight(width));
	}

	public Image toImage(int width, int height) {
		Image bImage = Image.createImage(width, height);

		Graphics g = bImage.getGraphics();

		g.setColor(0, 0, 0);
		g.fillRect(0, 0, bImage.getWidth(), bImage.getHeight());

		int y = 0;
		int x = 0;
		g.setColor(240, 240, 240);
		g.drawLine(0, y, bImage.getWidth(), y);
		y += 1;

		try {
			Image shield = null;
			shield = this.getShield();
			/*shield = Plus1Helper.resizeImage(
				shield,
				shield.getWidth(),
				bImage.getHeight()
			);*/

			g.drawImage(shield, x, y+(int)((bImage.getHeight()-shield.getHeight())/2), 0);
			x += shield.getWidth() + 2;
		} catch (IOException e) { /*_*/ }

		Font font = this.getFont();
		g.setFont(font);

		if (this.getTitle().length() > 0) {
			Vector list = this.separateString(this.getTitle(), width - x);

			for (int i = 0; i < list.size(); i++) {
				g.drawString(
					(String)list.elementAt(i),
					(int)((bImage.getWidth()/2)),
					y,
					Graphics.TOP | Graphics.HCENTER
				);
				y += font.getHeight();
			}
		}

		if (this.getContent().length() > 0) {
			Vector list = this.separateString(this.getContent(), width - x);

			for (int i = 0; i < list.size(); i++) {
				g.drawString(
					(String)list.elementAt(i),
					(int)((bImage.getWidth()/2)),
					y,
					Graphics.TOP | Graphics.HCENTER
				);
				y += font.getHeight();
			}
		}

		if (this.getImagePng() != null) {
			this.setRelativeX((int)((bImage.getWidth() - this.getImagePng().getWidth()) / 2));
			this.setRelativeY(y);

			if (!this.isAnimatedGif())
				g.drawImage(this.getImagePng(), this.getRelativeX(), this.getRelativeY(), 0);
		}

		return bImage;
	}

	// NOTE: attach after banner area painting
	public void attachGifPlayer(Canvas canvas) {
		try {
			this.player = Manager.createPlayer(this.getImagePath());
			this.player.realize();
			this.player.setLoopCount(-1);
			VideoControl vc = (VideoControl)this.player.getControl("VideoControl");
			if (vc != null) {
				vc.initDisplayMode(VideoControl.USE_DIRECT_VIDEO, canvas);
				vc.setDisplayLocation(
					this.getDisplayX() + this.getRelativeX(),
					this.getDisplayY() + this.getRelativeY()
				);
				vc.setVisible(true);
				this.player.start();
			}
		} catch (Exception e) { /*_*/ }
	}

	public void destroyGifPlayer() {
		if (this.player != null) {
			VideoControl vc = (VideoControl)this.player.getControl("VideoControl");
			if (vc != null)
				vc.setVisible(false);
			this.player.close();
		}
	}

	// FIXME: check gif for frames
	public boolean isAnimatedGif() {
		return
			this.getImagePath() != null
			&& this.getImagePath().endsWith("gif")
			&& Plus1Helper.hasNativeGifDecode();
	}

	public void setDisplayWidth(int width) {
		this.displayWidth = width;
	}

	public int getDisplayWidth() {
		return this.displayWidth;
	}

	public void setDisplayLocation(int x, int y) {
		this.displayX = x;
		this.displayY = y;
	}

	public int getDisplayX() {
		return this.displayX;
	}

	public int getDisplayY() {
		return this.displayY;
	}

	public void render(Graphics g) {
		g.drawImage(
			this.toImage(this.getDisplayWidth()),
			this.getDisplayX(),
			this.getDisplayY(),
			0
		);
	}

	public int getBannerImageHeight(int width) {
		int height = 0;
		Font font = this.getFont();

		int shieldHeight = 0;
		int shieldWidth = 0;

		try {
			shieldHeight = this.getShield().getHeight();
			shieldWidth = this.getShield().getWidth() + 2;
		} catch (Exception e) { /*_*/ }

		if (this.getTitle().length() > 0)
			height += font.getHeight() * this.separateString(this.getTitle(), width - shieldWidth).size();

		if (this.getContent().length() > 0)
			height += font.getHeight() * this.separateString(this.getContent(), width - shieldWidth).size();
		if (this.getImagePng() != null)
			height += this.getImagePng().getHeight();
			height += 3;

		return
			(height > shieldHeight)
				? height
				: shieldHeight;
	}

	public Form toForm(boolean addCommand) {
		Form form = new Form("Реклама plus1");

		this.appendToForm(form, addCommand);
		form.setCommandListener(this.getListener());

		return form;
	}

	public Plus1BannerCommand getCommand(String label, String longLabel, int priority) {
		Plus1BannerCommand command =
			new Plus1BannerCommand(label, longLabel, priority);

		command.setLink(this.getLink());

		return command;
	}

	public void appendToForm(Form form, boolean addCommand) {
		try {
			form.append(
				new ImageItem(
					null,
					this.getShield(),
					ImageItem.LAYOUT_LEFT | ImageItem.LAYOUT_TOP | ImageItem.LAYOUT_NEWLINE_AFTER,
					"шильдик"
				)
			);
		} catch (Exception e) { /*_*/ }

		form.append(
			new StringItem(null, this.getTitle() + "\n")
		);

		form.append(
			new StringItem(null, this.getContent() + "\n")
		);

		try {
			Item item;
			if (
				!this.isAnimatedGif()
				|| (item = this.getAnimatedImageItem()) == null
			)
				item = new ImageItem(
					null,
					this.getImagePng(),
					ImageItem.LAYOUT_CENTER,
					"картинка"
				);

			form.append(item);
		} catch (Exception e) { /*_*/ }

		if (addCommand) {
			form.addCommand(this.getCommand("Перейти", "Перейти по ссылке", 0));
		}
	}

	protected int getRelativeX() {
		return this.relativeX;
	}

	protected int getRelativeY() {
		return this.relativeY;
	}

	private void setRelativeX(int x) {
		this.relativeX = x;
	}

	private void setRelativeY(int y) {
		this.relativeY = y;
	}

	private Item getAnimatedImageItem() {
		Item item = null;

		try {
			Player p = Manager.createPlayer(this.getImagePath());
			p.realize();
			p.setLoopCount(-1);
			GUIControl gc = (GUIControl)p.getControl("GUIControl");
			if (gc != null) {
				item = (Item)gc.initDisplayMode(GUIControl.USE_GUI_PRIMITIVE, null);
				item.setLayout(ImageItem.LAYOUT_CENTER);
				p.start();
			}
		} catch (Exception e) { /*_*/ }

		return item;
	}

	private Image loadImage(String url) throws IOException {

		HttpConnection hpc = null;
		DataInputStream dis = null;
		Image image;
		try {
			hpc = (HttpConnection) Connector.open(url);
			int length = (int) hpc.getLength();
			byte[] data = new byte[length];
			dis = new DataInputStream(hpc.openInputStream());
			dis.readFully(data);

			image = Image.createImage(data, 0, data.length);
		} finally {
			if (hpc != null) {
				hpc.close();
			}
			if (dis != null) {
				dis.close();
			}
		}

		return image;
	}

	private Font getFont() {
		return Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
	}

	private Vector separateString(String inS, int width) {
		int maxlen = (width / this.getFont().charWidth('ш'));

		Vector list = new Vector();

		while (inS.indexOf(" ") != -1) {
			int spacer = inS.indexOf(" ");
			String part = inS.substring(0, spacer);
			list.addElement(part);
			inS = inS.substring(spacer + 1);
		}

		list.addElement(inS);

		for (int i = 1; i < list.size(); i++) {

			String prev = (String) list.elementAt(i - 1);
			String curr = (String) list.elementAt(i);

			if ((prev.length() + curr.length()) < maxlen) {
				list.setElementAt(prev + " " + curr, i);
				list.removeElementAt(i - 1);

				i--;
			}
		}

		return list;
	}
}
