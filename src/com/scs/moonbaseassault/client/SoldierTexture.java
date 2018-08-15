package com.scs.moonbaseassault.client;

import java.awt.Color;
import java.awt.Graphics2D;

import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.scs.stevetech1.jme.PaintableImage;

import ssmith.lang.NumberFunctions;

/**
 * origin is top-left
 * Rows (from top):
 * 0 - Skin tone
 * 1 - Eyes and eyebrows
 * 2 - Hair
 * 3 - Shirt
 * 4 - Trousers
 *
 */
public class SoldierTexture {

	private static final int SIZE = 32;

	public SoldierTexture() {
		super();
	}


	public static Texture getTexture(int side, boolean player) {
		PaintableImage pi = new PaintableImage(SIZE, SIZE) {

			@Override
			public void paint(Graphics2D g) {
				for (int row=0 ; row<5 ; row++) {
					switch (row) {
					case 0: // trousers
						switch (side) {
						case 1:
							g.setColor(Color.yellow.darker());
							break;
						case 2:
							g.setColor(Color.green.darker());
							break;
						default:
							throw new RuntimeException("Invalid side:" + side);
						}
						if (!player) {
							g.setColor(g.getColor().darker().darker());
						}
						break;

					case 1: // Shirt
						switch (side) {
						case 1:
							g.setColor(Color.yellow);
							break;
						case 2:
							g.setColor(Color.green);
							break;
						default:
							throw new RuntimeException("Invalid side:" + side);
						}
						if (!player) {
							g.setColor(g.getColor().darker());
						}
						break;

					case 2: // Hair
						g.setColor(getRandomHairColour(player));
						break;

					case 3: // Eyes
						g.setColor(Color.LIGHT_GRAY);
						break;

					case 4: // Skin
						g.setColor(getRandomSkinColour());
						break;
					}

					int sy = getRowStart(row);
					int ey = getRowStart(row+1)-1;
					g.fillRect(0, sy, SIZE, ey);
				}
			}

		};

		pi.refreshImage();
		return new Texture2D(pi);
	}


	private static int getRowStart(int row) {
		switch (row) {
		case 0:
			return 0;
		case 1:
			return 10;
		case 2:
			return 15;
		case 3:
			return 21;
		case 4:
			return 26;
		case 5:
			return 31;
		default:
			throw new IllegalArgumentException("Invalid row:" + row);
		}
	}


	private static Color getRandomHairColour(boolean player) {
		if (player) {
			return Color.black;
		} else {
			return Color.LIGHT_GRAY;
		}
	}


	private static Color getRandomSkinColour() {
		int i = NumberFunctions.rnd(1, 2);
		switch (i) {
		case 1:
			return Color.pink;
		case 2:
			return Color.pink; // todo
		default:
			throw new IllegalArgumentException(""+i);
		}
	}

}

