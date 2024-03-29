package com.scs.moonbaseassault.client.hud;

import java.awt.Color;
import java.awt.Graphics2D;

import com.jme3.math.Vector3f;
import com.scs.moonbaseassault.MoonbaseAssaultGlobals;
import com.scs.moonbaseassault.entities.MA_AISoldier;
import com.scs.moonbaseassault.server.MapLoader;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractOtherPlayersAvatar;
import com.scs.stevetech1.jme.PaintableImage;

public class MapImageTexture extends PaintableImage {

	private static final float ALPHA = 1f; //0 = transparent

	private int[][] data;
	private int pixelSize; // How big each map square is on the scanner
	private AbstractGameClient client;

	public MapImageTexture(int sizeInPixels, int _pixelSize, AbstractGameClient _client) {
		super(sizeInPixels, sizeInPixels);

		pixelSize = Math.max(1, _pixelSize);
		client = _client;

		refreshImage();
	}


	public void setMapData(int[][] _data) {
		data = _data;
		this.refreshImage();
	}


	@Override
	public void paint(Graphics2D g) {
		g.setBackground(new Color(0f, 0f, 0f, 0f));
		g.clearRect(0, 0, getWidth(), getHeight());

		// Origin is bottom-left
		if (data != null) {
			// Map walls
			for (int y=0 ; y<data.length ; y++) {
				for (int x=0 ; x<data[0].length ; x++) {
					if (data[x][y] == MapLoader.WALL) {
						g.setColor(new Color(0f, 1f, 0f, 0.5f)); // green
						paintSquare(g, x, y, 1);
					} else if (data[x][y] == MapLoader.COMPUTER) {
						g.setColor(new Color(1f, 1f, 1f, ALPHA)); // White
						paintSquare(g, x, y, 1);
					} else if (data[x][y] == MapLoader.DESTROYED_COMPUTER) {
						g.setColor(new Color(.3f, .3f, .3f, ALPHA));
						paintSquare(g, x, y, 1);
					}
				}
			}

			for (IEntity e : client.entitiesForProcessing) {
				if (e instanceof MA_AISoldier) {
					MA_AISoldier ai = (MA_AISoldier)e;
					if (ai.getSide() == client.side || MoonbaseAssaultGlobals.SHOW_ALL_UNITS_ON_HUD) {
						Vector3f pos = ai.getWorldTranslation();
						if (ai.getSide() == client.side) {
							g.setColor(new Color(1f, 1f, 0f, ALPHA)); // Yellow

						} else {
							g.setColor(new Color(1f, 0f, 0f, ALPHA)); // Red
						}
						paintSquare(g, (int)pos.x, (int)pos.z, 1);
					}
				} else if (e instanceof AbstractAvatar) {
					g.setColor(new Color(1f, 1f, 1f, ALPHA)); // White
					AbstractAvatar ai = (AbstractAvatar)e;
					Vector3f pos = ai.getWorldTranslation();
					paintSquare(g, (int)pos.x, (int)pos.z, 1);
				} else if (e instanceof AbstractOtherPlayersAvatar) {
					AbstractOtherPlayersAvatar avatar = (AbstractOtherPlayersAvatar)e;
					if (avatar.getSide() == client.side || MoonbaseAssaultGlobals.SHOW_ALL_UNITS_ON_HUD) {
						Vector3f pos = avatar.getWorldTranslation();
						if (avatar.getSide() == client.side) {
							g.setColor(new Color(1f, 1f, 0f, ALPHA)); // Yellow
						} else {
							g.setColor(new Color(1f, 0f, 0f, ALPHA)); // Red
						}
						paintSquare(g, (int)pos.x, (int)pos.z, 2);
					}
				}
			}
		}
	}


	private void paintSquare(Graphics2D g, int mx, int my, int pxlSizeMult) {
		int x = (mx)*pixelSize;
		int y = (data[0].length-1-my)*pixelSize;
		int size = Math.max(pixelSize*pxlSizeMult, pxlSizeMult);
		g.fillRect(x, y, size, size);
	}

}
