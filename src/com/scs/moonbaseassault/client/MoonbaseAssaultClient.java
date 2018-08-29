package com.scs.moonbaseassault.client;

import com.jme3.audio.AudioData.DataType;
import com.jme3.audio.AudioNode;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.scs.moonbaseassault.MoonbaseAssaultGlobals;
import com.scs.moonbaseassault.client.hud.MoonbaseAssaultHUD;
import com.scs.moonbaseassault.client.modules.ConnectModule;
import com.scs.moonbaseassault.client.modules.DisconnectedModule;
import com.scs.moonbaseassault.client.modules.IModule;
import com.scs.moonbaseassault.client.modules.IntroJonlan;
import com.scs.moonbaseassault.client.modules.IntroModule;
import com.scs.moonbaseassault.client.modules.MainModule;
import com.scs.moonbaseassault.client.modules.PreGameModule;
import com.scs.moonbaseassault.netmessages.HudDataMessage;
import com.scs.moonbaseassault.server.MoonbaseAssaultServer;
import com.scs.moonbaseassault.shared.MoonbaseAssaultCollisionValidator;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.client.ValidClientSettings;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.data.SimpleGameData;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.hud.AbstractHUDImage;
import com.scs.stevetech1.netmessages.MyAbstractMessage;
import com.scs.stevetech1.netmessages.NewEntityData;
import com.scs.stevetech1.server.Globals;

import ssmith.util.MyProperties;

public final class MoonbaseAssaultClient extends AbstractGameClient {

	private MoonbaseAssaultClientEntityCreator entityCreator;
	private AbstractHUDImage currentHUDTextImage;
	public MoonbaseAssaultHUD hud;
	private MoonbaseAssaultCollisionValidator collisionValidator;

	private final String ipAddress;
	private final int port;

	private IModule currentModule;
	private AudioNode musicNode;

	public static void main(String[] args) {
		try {
			MyProperties props = null;
			if (args.length > 0) {
				props = new MyProperties(args[0]);
			} else {
				props = new MyProperties();
				Globals.p("Warning: No config file specified");
			}
			final String gameIpAddress = props.getPropertyAsString("gameIpAddress", "localhost"); //"www.stellarforces.com");
			final int gamePort = props.getPropertyAsInt("gamePort", MoonbaseAssaultGlobals.PORT);

			final int tickrateMillis = props.getPropertyAsInt("tickrateMillis", 25);
			final int clientRenderDelayMillis = props.getPropertyAsInt("clientRenderDelayMillis", 200);
			final int timeoutMillis = props.getPropertyAsInt("timeoutMillis", 100000);

			final float mouseSensitivity = props.getPropertyAsFloat("mouseSensitivity", 1f);

			new MoonbaseAssaultClient(gameIpAddress, gamePort,
					tickrateMillis, clientRenderDelayMillis, timeoutMillis,
					mouseSensitivity);
		} catch (Exception e) {
			Globals.p("Error: " + e);
			e.printStackTrace();
		}
	}


	private MoonbaseAssaultClient(String gameIpAddress, int gamePort, 
			int tickrateMillis, int clientRenderDelayMillis, int timeoutMillis,
			float mouseSensitivity) {
		super(new ValidClientSettings(MoonbaseAssaultServer.GAME_ID, "key", 1), "Moonbase Assault", null, 
				tickrateMillis, clientRenderDelayMillis, timeoutMillis, mouseSensitivity); 

		ipAddress = gameIpAddress;
		port = gamePort;

		start();
	}


	@Override
	public void simpleInitApp() {
		super.simpleInitApp();

		entityCreator = new MoonbaseAssaultClientEntityCreator();
		collisionValidator = new MoonbaseAssaultCollisionValidator();

		this.getViewPort().setBackgroundColor(ColorRGBA.Black);

		playMusic();

		hud = new MoonbaseAssaultHUD(this, this.getCamera());

		if (Globals.RELEASE_MODE) {
			this.setModule(new IntroModule(this));
		} else {
			this.startConnectToServerModule();
		}

	}


	private void playMusic() {
		try {
			musicNode = new AudioNode(assetManager, "Sounds/n-Dimensions (Main Theme - Retro Ver.ogg", DataType.Stream);
			musicNode.setPositional(false);
			this.getRootNode().attachChild(musicNode);
			musicNode.play();
		} catch (java.lang.IllegalStateException ex) {
			// Unable to play sounds - no audiocard/speakers?
		}
	}


	public void startJonlanModule() {
		this.setModule(new IntroJonlan(this));
	}


	public void startConnectToServerModule() {
		this.setModule(new ConnectModule(this, ipAddress, port));
	}


	public void showPreGameModule() {
		this.setModule(new PreGameModule(this));
	}


	public void startMainModule() {
		try {
			this.musicNode.stop();
		} catch (IllegalStateException ex) {
			// Unable to play music
		}
		this.setModule(new MainModule(this));
	}


	@Override
	public void disconnected() {
		this.setModule(new DisconnectedModule(this));
	}


	private void setModule(IModule m) {
		if (this.currentModule != null) {
			this.currentModule.destroy();
		}
		
		m.simpleInit();
		this.currentModule = m;
		
		if (currentModule instanceof MainModule) {
			if (this.hud.getParent() == null) {
				this.guiNode.attachChild(hud); // Re-add since the previous module probably cleared out the guiNode
			}
		}
	}


	@Override
	public int getGameID() {
		return this.gameData.gameID;
	}


	@Override
	protected void setUpLight() {
		// Light is set up in modules
	}


	@Override
	public void simpleUpdate(float tpfSecs) {
		super.simpleUpdate(tpfSecs);
		this.currentModule.simpleUpdate(tpfSecs);
		this.hud.processByClient(this, tpfSecs);
	}


	@Override
	protected boolean handleMessage(MyAbstractMessage message) {
		if (message instanceof HudDataMessage) {
			HudDataMessage hdm = (HudDataMessage) message;
			this.hud.setMapData(hdm.scannerData);
			this.hud.setCompsDestroyed(hdm.compsDestroyed);
			return true;
		} else {
			return super.handleMessage(message);
		}
	}


	@Override
	protected void receivedWelcomeMessage() {
		// Do nothing for now, don't join game yet.
	}


	@Override
	public boolean canCollide(PhysicalEntity a, PhysicalEntity b) {
		return this.collisionValidator.canCollide(a, b);
	}


	@Override
	public void collisionOccurred(SimpleRigidBody<PhysicalEntity> a, SimpleRigidBody<PhysicalEntity> b) {
		super.collisionOccurred(a, b);
	}


	@Override
	protected IEntity actuallyCreateEntity(AbstractGameClient client, NewEntityData msg) {
		return entityCreator.createEntity(client, msg);
	}


	@Override
	protected void playerHasWon() {
		removeCurrentHUDTextImage();
		int width = this.cam.getWidth()/2;
		int height = this.cam.getHeight()/2;
		int x = (this.cam.getWidth()/2)-(width/2);
		int y = (int)(this.cam.getHeight() * 0.5f);
		currentHUDTextImage = new AbstractHUDImage(this, this.getNextEntityID(), this.getGuiNode(), "Textures/text/victory.png", x, y, width, height, 5);
	}


	@Override
	protected void playerHasLost() {
		removeCurrentHUDTextImage();
		int width = this.cam.getWidth()/2;
		int height = this.cam.getHeight()/2;
		int x = (this.cam.getWidth()/2)-(width/2);
		int y = (int)(this.cam.getHeight() * 0.5f);
		currentHUDTextImage = new AbstractHUDImage(this, this.getNextEntityID(), this.getGuiNode(), "Textures/text/defeat.png", x, y, width, height, 5);
	}


	@Override
	protected void gameIsDrawn() {
		removeCurrentHUDTextImage();
		int width = this.cam.getWidth()/2;
		int height = this.cam.getHeight()/2;
		int x = (this.cam.getWidth()/2)-(width/2);
		int y = (int)(this.cam.getHeight() * 0.5f);
		currentHUDTextImage = new AbstractHUDImage(this, this.getNextEntityID(), this.getGuiNode(), "Textures/text/defeat.png", x, y, width, height, 5);
	}


	@Override
	protected void gameStatusChanged(int oldStatus, int newStatus) {
		final int width = this.cam.getWidth()/5;
		final int height = this.cam.getHeight()/5;
		final int x = (this.cam.getWidth()/2)-(width/2);
		final int y = (int)(this.cam.getHeight() * 0.8f);

		switch (newStatus) {
		case SimpleGameData.ST_WAITING_FOR_PLAYERS:
			removeCurrentHUDTextImage();
			currentHUDTextImage = new AbstractHUDImage(this, this.getNextEntityID(), this.getGuiNode(), "Textures/text/waitingforplayers.png", x, y, width, height, 3);
			break;
		case SimpleGameData.ST_DEPLOYING:
			removeCurrentHUDTextImage();
			currentHUDTextImage = new AbstractHUDImage(this, this.getNextEntityID(), this.getGuiNode(), "Textures/text/getready.png", x, y, width, height, 3);
			break;
		case SimpleGameData.ST_STARTED:
			removeCurrentHUDTextImage();
			currentHUDTextImage = new AbstractHUDImage(this, this.getNextEntityID(), this.getGuiNode(), "Textures/text/missionstarted.png", x, y, width, height, 3);
			break;
		case SimpleGameData.ST_FINISHED:
			// Don't show anything, this will be handled with a win/lose message
			break;
		default:
			// DO nothing
		}

	}


	private void removeCurrentHUDTextImage() {
		if (this.currentHUDTextImage != null) {
			if (currentHUDTextImage.getParent() != null) {
				currentHUDTextImage.remove();
			}
			currentHUDTextImage = null;
		}
	}

	/*
	@Override
	protected Spatial getPlayersWeaponModel() {
		Spatial model = assetManager.loadModel("Models/pistol/pistol.blend");
		JMEModelFunctions.setTextureOnSpatial(assetManager, model, "Models/pistol/pistol_tex.png");
		model.scale(0.1f);
		// x moves l-r, z moves further away
		//model.setLocalTranslation(-0.20f, -.2f, 0.4f);
		//model.setLocalTranslation(-0.15f, -.2f, 0.2f);
		model.setLocalTranslation(-0.10f, -.15f, 0.2f);
		return model;
	}
	 */

	@Override
	protected Class[] getListofMessageClasses() {
		return new Class[] {HudDataMessage.class};
	}



	/**
	 * Override if required
	 */
	public Node getHudNode() {
		return this.hud;
	}


	/**
	 * Override if required
	 */
	protected void showDamageBox() {
		hud.showDamageBox();
	}


	/**
	 * Override if required
	 */
	protected void showMessage(String msg) {
		hud.appendToLog(msg);
	}


	/**
	 * Override if required
	 */
	protected void appendToLog(String msg) {
		hud.appendToLog(msg);
	}


}
