package com.scs.moonbaseassault.client;

import com.jme3.math.ColorRGBA;
import com.jme3.scene.Spatial;
import com.scs.moonbaseassault.client.hud.MoonbaseAssaultHUD;
import com.scs.moonbaseassault.client.modules.IModule;
import com.scs.moonbaseassault.client.modules.IntroModule;
import com.scs.moonbaseassault.client.modules.MainModule;
import com.scs.moonbaseassault.netmessages.HudDataMessage;
import com.scs.moonbaseassault.server.MoonbaseAssaultServer;
import com.scs.moonbaseassault.shared.MoonbaseAssaultCollisionValidator;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.data.SimpleGameData;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.hud.AbstractHUDImage;
import com.scs.stevetech1.hud.IHUD;
import com.scs.stevetech1.jme.JMEModelFunctions;
import com.scs.stevetech1.netmessages.MyAbstractMessage;
import com.scs.stevetech1.netmessages.NewEntityData;
import com.scs.stevetech1.server.Globals;

import ssmith.util.MyProperties;

public class MoonbaseAssaultClient extends AbstractGameClient {

	private MoonbaseAssaultClientEntityCreator entityCreator;
	private AbstractHUDImage currentHUDTextImage;
	public MoonbaseAssaultHUD hud;
	private MoonbaseAssaultCollisionValidator collisionValidator;
	
	private String ipAddress;
	private int port;

	private IModule currentModule;
	private MainModule mainModule;

	public static void main(String[] args) {
		try {
			MyProperties props = null;
			if (args.length > 0) {
				props = new MyProperties(args[0]);
			} else {
				props = new MyProperties();
				Globals.p("Warning: No config file specified");
			}
			String gameIpAddress = props.getPropertyAsString("gameIpAddress", "localhost"); //"www.stellarforces.com");
			int gamePort = props.getPropertyAsInt("gamePort", 6145);

			int tickrateMillis = props.getPropertyAsInt("tickrateMillis", 25);
			int clientRenderDelayMillis = props.getPropertyAsInt("clientRenderDelayMillis", 200);
			int timeoutMillis = props.getPropertyAsInt("timeoutMillis", 100000);

			float mouseSensitivity = props.getPropertyAsFloat("mouseSensitivity", 1f);

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
		super(MoonbaseAssaultServer.GAME_ID, "key", "Moonbase Assault", null, 
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

		//getGameNode().attachChild(SkyFactory.createSky(getAssetManager(), "Textures/BrightSky.dds", SkyFactory.EnvMapType.CubeMap));

		this.setModule(new IntroModule(this));
		this.mainModule = new MainModule(this, ipAddress, port);
	}


	private void setModule(IModule m) {
		if (this.currentModule != null) {
			this.currentModule.destroy();
		}
		m.simpleInit();
		this.currentModule = m;
	}
	
	
	public void setMainModule() {
		this.setModule(this.mainModule);
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
	public void simpleUpdate(float tpf_secs) {
		super.simpleUpdate(tpf_secs);
		this.currentModule.simpleUpdate(tpf_secs);
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
		currentHUDTextImage = new AbstractHUDImage(this, this.getNextEntityID(), this.hud.getRootNode(), "Textures/text/victory.png", x, y, width, height, 5);
	}


	@Override
	protected void playerHasLost() {
		removeCurrentHUDTextImage();
		int width = this.cam.getWidth()/2;
		int height = this.cam.getHeight()/2;
		int x = (this.cam.getWidth()/2)-(width/2);
		int y = (int)(this.cam.getHeight() * 0.5f);
		currentHUDTextImage = new AbstractHUDImage(this, this.getNextEntityID(), this.hud.getRootNode(), "Textures/text/defeat.png", x, y, width, height, 5);
	}


	@Override
	protected void gameIsDrawn() {
		removeCurrentHUDTextImage();
		int width = this.cam.getWidth()/2;
		int height = this.cam.getHeight()/2;
		int x = (this.cam.getWidth()/2)-(width/2);
		int y = (int)(this.cam.getHeight() * 0.5f);
		currentHUDTextImage = new AbstractHUDImage(this, this.getNextEntityID(), this.hud.getRootNode(), "Textures/text/defeat.png", x, y, width, height, 5);
	}


	@Override
	protected IHUD createAndGetHUD() {
		hud = new MoonbaseAssaultHUD(this, this.getCamera());
		return hud;
	}


	@Override
	protected void gameStatusChanged(int oldStatus, int newStatus) {
		int width = this.cam.getWidth()/5;
		int height = this.cam.getHeight()/5;
		int x = (this.cam.getWidth()/2)-(width/2);
		int y = (int)(this.cam.getHeight() * 0.8f);

		switch (newStatus) {
		case SimpleGameData.ST_WAITING_FOR_PLAYERS:
			removeCurrentHUDTextImage();
			currentHUDTextImage = new AbstractHUDImage(this, this.getNextEntityID(), this.hud.getRootNode(), "Textures/text/waitingforplayers.png", x, y, width, height, 3);
			break;
		case SimpleGameData.ST_DEPLOYING:
			removeCurrentHUDTextImage();
			currentHUDTextImage = new AbstractHUDImage(this, this.getNextEntityID(), this.hud.getRootNode(), "Textures/text/getready.png", x, y, width, height, 3);
			break;
		case SimpleGameData.ST_STARTED:
			removeCurrentHUDTextImage();
			currentHUDTextImage = new AbstractHUDImage(this, this.getNextEntityID(), this.hud.getRootNode(), "Textures/text/missionstarted.png", x, y, width, height, 3);
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


	@Override
	protected Spatial getPlayersWeaponModel() {
		//if (!Globals.HIDE_BELLS_WHISTLES) {
		Spatial model = assetManager.loadModel("Models/pistol/pistol.blend");
		JMEModelFunctions.setTextureOnSpatial(assetManager, model, "Models/pistol/pistol_tex.png");
		model.scale(0.1f);
		// x moves l-r, z moves further away
		//model.setLocalTranslation(-0.20f, -.2f, 0.4f);
		//model.setLocalTranslation(-0.15f, -.2f, 0.2f);
		model.setLocalTranslation(-0.10f, -.15f, 0.2f);
		return model;
		/*} else {
			return null;
		}*/
	}


	@Override
	protected Class[] getListofMessageClasses() {
		return new Class[] {HudDataMessage.class};
	}


	@Override
	public void onAction(String name, boolean value, float tpf) {
		if (name.equalsIgnoreCase(TEST)) {
			if (value) {
			}
		} else {
			super.onAction(name, value, tpf);
		}
	}


}
