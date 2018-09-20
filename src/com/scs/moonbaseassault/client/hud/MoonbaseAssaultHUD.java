package com.scs.moonbaseassault.client.hud;

import java.util.LinkedList;

import com.atr.jme.font.TrueTypeFont;
import com.atr.jme.font.TrueTypeMesh;
import com.atr.jme.font.asset.TrueTypeKeyMesh;
import com.atr.jme.font.asset.TrueTypeLoader;
import com.atr.jme.font.shape.TrueTypeContainer;
import com.atr.jme.font.util.StringContainer;
import com.atr.jme.font.util.Style;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.ui.Picture;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.data.SimpleGameData;

import ssmith.util.RealtimeInterval;

public class MoonbaseAssaultHUD extends Node {

	private static final int MAX_LINES = 6;

	private RealtimeInterval updateHudTextInterval = new RealtimeInterval(1000);

	private Camera cam;
	private Geometry damage_box;
	private ColorRGBA dam_box_col = new ColorRGBA(1, 0, 0, 0.0f);
	private boolean process_damage_box;
	private AbstractGameClient game;

	private HUDMapImage hudMapImage;

	private TrueTypeContainer abilityGun, abilityOther, healthText; // Update instantly 
	private String debugText, gameStatus, gameTime, pingText, compsDestroyedText, numPlayers, gameID;
	private TrueTypeContainer textArea; // For showing all other stats 
	private TrueTypeContainer log;
	private LinkedList<String> logLines = new LinkedList<>();

	public MoonbaseAssaultHUD(AbstractGameClient _game, Camera _cam) { 
		super("HUD");

		game = _game;
		cam = _cam;

		_game.getAssetManager().registerLoader(TrueTypeLoader.class, "ttf");
		float fontSize = cam.getWidth() / 40; 
		TrueTypeKeyMesh ttkSmall = new TrueTypeKeyMesh("Fonts/SF Distant Galaxy.ttf", Style.Plain, (int)fontSize);
		TrueTypeFont ttfSmall = (TrueTypeMesh)_game.getAssetManager().loadAsset(ttkSmall);
		TrueTypeKeyMesh ttkLarge = new TrueTypeKeyMesh("Fonts/SF Distant Galaxy.ttf", Style.Plain, (int)fontSize*2);
		TrueTypeFont ttfLarge = (TrueTypeMesh)_game.getAssetManager().loadAsset(ttkLarge);
		float lineSpacing = cam.getHeight() / 30;

		super.setLocalTranslation(0, 0, 0);

		this.addTargetter();

		float xPos = cam.getWidth() * .7f;

		textArea = ttfSmall.getFormattedText(new StringContainer(ttfSmall, "Hello World"), ColorRGBA.Green);
		textArea.setLocalTranslation(xPos, (int)(cam.getHeight()*.6f), 0);
		this.attachChild(textArea);
		//textArea.setText("Waiting for data...");

		float yPos = cam.getHeight() - lineSpacing;

		yPos -= lineSpacing;
		abilityGun = ttfSmall.getFormattedText(new StringContainer(ttfSmall, "Hello World"), ColorRGBA.Green);
		abilityGun.setLocalTranslation(xPos, yPos, 0);
		this.attachChild(abilityGun);
/*
		yPos -= lineSpacing;
		abilityOther = ttfSmall.getFormattedText(new StringContainer(ttfSmall, "Hello World"), ColorRGBA.Green);
		abilityOther.setLocalTranslation(xPos, yPos, 0);
		this.attachChild(abilityOther);
*/
		yPos -= lineSpacing;
		healthText = ttfSmall.getFormattedText(new StringContainer(ttfSmall, "Hello World"), ColorRGBA.Green);
		healthText.setLocalTranslation(xPos, yPos, 0);
		this.attachChild(healthText);

		log = ttfSmall.getFormattedText(new StringContainer(ttfSmall, "Hello World"), ColorRGBA.Green);
		log.setLocalTranslation(20, cam.getHeight()-20, 0);
		this.attachChild(log);

		// Damage box
		{
			Material mat = new Material(game.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
			mat.setColor("Color", this.dam_box_col);
			mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
			damage_box = new Geometry("damagebox", new Quad(cam.getWidth(), cam.getHeight()));
			damage_box.move(0, 0, 0);
			damage_box.setMaterial(mat);
			this.attachChild(damage_box);
		}

		this.setDebugText("");

		this.updateGeometricState();

		this.updateModelBound();

	}


	public void processByClient(AbstractGameClient client, float tpf) {
		if (updateHudTextInterval.hitInterval()) {
			if (client.gameData != null) {
				this.gameID = "Game ID: " + client.gameData.gameID;
				this.setGameStatus(SimpleGameData.getStatusDesc(client.gameData.getGameStatus()));
				if (client.gameData.isInGame()) {
					this.setGameTime(client.gameData.getTime(client.serverTime));
				} else {
					this.setGameTime("");
				}
				if (client.playersList != null) {
					this.setNumPlayers(client.playersList.size());
				}
			}
			this.setPing(client.pingRTT);
			this.updateTextArea();

			if (client.currentAvatar != null) {
				this.setHealthText((int)client.currentAvatar.getHealth());
				// These must be after we might use them, so the hud is correct
				if (client.currentAvatar.ability[0] != null) {
					setAbilityGunText(client.currentAvatar.ability[0].getHudText());
				}
				if (client.currentAvatar.ability[1] != null) {
					setAbilityOtherText(client.currentAvatar.ability[1].getHudText());
				}
			}
			
			if (hudMapImage != null) {
				this.hudMapImage.refreshImage();
			}
		}

		if (process_damage_box) {
			this.dam_box_col.a -= (tpf/2);
			if (dam_box_col.a <= 0) {
				dam_box_col.a = 0;
				process_damage_box = false;
			}
		}

	}


	public void appendToLog(String s) {
		this.logLines.add(s);
		while (this.logLines.size() > MAX_LINES) {
			this.logLines.remove(0);
		}
		StringBuilder str = new StringBuilder();
		for(String line : this.logLines) {
			str.append(line + "\n");
		}
		this.log.setText(str.toString());
		this.log.updateGeometry();
	}


	private void updateTextArea() {
		StringBuilder str = new StringBuilder();
		str.append(this.debugText + "\n");
		str.append(this.gameID + "\n");
		str.append(this.gameStatus + "\n");
		str.append(this.gameTime + "\n");
		str.append(this.pingText + "\n");
		str.append(this.compsDestroyedText + "\n");
		str.append(this.numPlayers + "\n");
		this.textArea.setText(str.toString());
		this.textArea.updateGeometry();
	}


	public void setDebugText(String s) {
		this.debugText = s;
	}


	private void setGameStatus(String s) {
		this.gameStatus = "Game Status: " + s;
	}


	private void setGameTime(String s) {
		this.gameTime = s;
	}


	public void setAbilityGunText(String s) {
		this.abilityGun.setText(s);
		this.abilityGun.updateGeometry();
	}


	public void setAbilityOtherText(String s) {
		this.abilityOther.setText(s);
		this.abilityOther.updateGeometry();
	}


	public void setHealthText(int s) {
		this.healthText.setText("Health: " + s);
		this.healthText.updateGeometry();
	}


	public void setCompsDestroyed(int s) {
		this.compsDestroyedText = "CPUs Destroyed: " + s;
	}


	private void setPing(long i) {
		this.pingText = "Ping: " + i;
	}


	private void setNumPlayers(int i) {
		this.numPlayers = "Num Players: " + i;
	}


	public void showDamageBox() {
		process_damage_box = true;
		this.dam_box_col.a = .5f;
		this.dam_box_col.r = 1f;
		this.dam_box_col.g = 0f;
		this.dam_box_col.b = 0f;
	}


	public void showCollectBox() {
		process_damage_box = true;
		this.dam_box_col.a = .3f;
		this.dam_box_col.r = 0f;
		this.dam_box_col.g = 1f;
		this.dam_box_col.b = 1f;
	}


	private void addTargetter() {
		Picture targetting_reticule = new Picture("HUD Picture");
		targetting_reticule.setImage(game.getAssetManager(), "Textures/centre_crosshairs.png", true);
		float crosshairs_w = cam.getWidth()/10;
		targetting_reticule.setWidth(crosshairs_w);
		float crosshairs_h = cam.getHeight()/10;
		targetting_reticule.setHeight(crosshairs_h);
		targetting_reticule.setLocalTranslation((cam.getWidth() - crosshairs_w)/2, (cam.getHeight() - crosshairs_h)/2, 0);
		this.attachChild(targetting_reticule);
	}


	public void setMapData(int scannerData[][]) {
		if (this.hudMapImage == null) {
			this.addMapImage(scannerData.length);
		}
		this.hudMapImage.mapImageTex.setMapData(scannerData);
	}


	private HUDMapImage addMapImage(int mapSize) {
		float sizeInPixels = Math.max(cam.getWidth()/3, mapSize);
		hudMapImage = new HUDMapImage(game.getAssetManager(), (int)sizeInPixels, mapSize, game);
		hudMapImage.setWidth(sizeInPixels);
		hudMapImage.setHeight(sizeInPixels);
		hudMapImage.setLocalTranslation((cam.getWidth() - sizeInPixels)/2, 0, 0);
		this.attachChild(hudMapImage);
		return hudMapImage;
	}


}
