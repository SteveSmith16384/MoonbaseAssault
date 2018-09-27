package com.scs.moonbaseassault.server;

import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.jme3.system.JmeContext;
import com.scs.moonbaseassault.MASimplePlayerData;
import com.scs.moonbaseassault.MoonbaseAssaultGlobals;
import com.scs.moonbaseassault.entities.MA_AISoldier;
import com.scs.moonbaseassault.entities.SoldierServerAvatar;
import com.scs.moonbaseassault.entities.Spaceship1;
import com.scs.moonbaseassault.netmessages.HudDataMessage;
import com.scs.moonbaseassault.shared.MoonbaseAssaultCollisionValidator;
import com.scs.moonbaseassault.shared.MoonbaseAssaultGameData;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.client.ValidateClientSettings;
import com.scs.stevetech1.data.GameOptions;
import com.scs.stevetech1.data.SimpleGameData;
import com.scs.stevetech1.data.SimplePlayerData;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.jme.JMEAngleFunctions;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.server.Globals;

import ssmith.astar.IAStarMapInterface;
import ssmith.lang.Functions;
import ssmith.lang.NumberFunctions;
import ssmith.util.MyProperties;

public class MoonbaseAssaultServer extends AbstractGameServer implements IAStarMapInterface {

	private static final int COMPS_DESTROYED_TO_WIN = 10;
	public static final boolean PLAYERS_ARE_ALWAYS_DEFENDERS = false;

	public static final String GAME_ID = "Moonbase Assault";

	public static final float CEILING_HEIGHT = 1.4f;
	public static final float LASER_DIAM = 0.02f;

	private static long deployDurationSecs, gameDurationSecs, restartDurationSecs;

	private int mapData[][]; // Also used to tell the client what the scanner should show
	private List<Point> computerSquares; // For A*
	public ArrayList<Point>[] deploySquares;
	private MoonbaseAssaultCollisionValidator collisionValidator = new MoonbaseAssaultCollisionValidator();
	private byte winningSide = MoonbaseAssaultGlobals.SIDE_DEFENDERS; // Defenders win by default
	private CreateUnitsSystem createUnitsSystem;
	private MoonbaseAssaultGameData maGameData;
	private boolean nextPlayerIsDefender = true;

	public static void main(String[] args) {
		try {
			MyProperties props = null;
			if (args.length > 0) {
				props = new MyProperties(args[0]);
			} else {
				props = new MyProperties();
				Globals.p("No config file specified.  Using defaults.");
			}
			String gameIpAddress = props.getPropertyAsString("gameIpAddress", "localhost");
			int gamePort = props.getPropertyAsInt("gamePort", MoonbaseAssaultGlobals.PORT);
			
			deployDurationSecs = props.getPropertyAsInt("deployDurationSecs", 10);
			gameDurationSecs = props.getPropertyAsInt("gameDurationSecs", 240);
			restartDurationSecs = props.getPropertyAsInt("restartDurationSecs", 10);

			new MoonbaseAssaultServer(gameIpAddress, gamePort);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private MoonbaseAssaultServer(String gameIpAddress, int gamePort) throws IOException {
		super(new ValidateClientSettings(GAME_ID, 1d, "key"), 
				new GameOptions(Globals.DEFAULT_TICKRATE, Globals.DEFAULT_SEND_UPDATES_INTERVAL, Globals.DEFAULT_RENDER_DELAY, Globals.DEFAULT_NETWORK_TIMEOUT, 
				deployDurationSecs*1000, gameDurationSecs*1000, restartDurationSecs*1000, 
				gameIpAddress, gamePort, 
				10, 5));

		start(JmeContext.Type.Headless);
	}


	@Override
	public void simpleInitApp() {
		try {
			String text = Functions.readAllFileFromJar("serverdata/ai_names.txt");
			String[] lines = text.split("\n");
			createUnitsSystem = new CreateUnitsSystem(this, lines);
		} catch (Exception e) {
			throw new RuntimeException("Error loading names", e);
		}

		super.physicsController.setStepForce(MoonbaseAssaultGlobals.STEP_FORCE);
		super.physicsController.setRampForce(MoonbaseAssaultGlobals.RAMP_FORCE);

		super.simpleInitApp();
	}


	@Override
	public void simpleUpdate(float tpfSecs) {
		super.simpleUpdate(tpfSecs);

		if (!Globals.TEST_AI && !Globals.NO_AI_UNITS) {
			if (this.gameData.isInGame()) {
				this.createUnitsSystem.process();
			}
		}
	}


	@Override
	public void moveAvatarToStartPosition(AbstractAvatar avatar) {
		this.moveAISoldierToStartPosition(avatar, avatar.side);
	}


	@Override
	protected void createGame() {
		this.maGameData = new MoonbaseAssaultGameData();

		MapLoader map = new MapLoader(this);
		try {
			//map.loadMap("serverdata/moonbaseassault_small.csv");
			map.loadMap("serverdata/moonbaseassault.csv");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}


		mapData = map.scannerData;
		this.sendMessageToInGameClients(new HudDataMessage(this.mapData, this.maGameData.computersDestroyed));
		this.deploySquares = map.deploySquares;

		this.computerSquares = new ArrayList<Point>();
		for (int y=0 ; y<mapData.length ; y++) {
			for (int x=0 ; x<mapData.length ; x++) {
				if (this.mapData[x][y] == MapLoader.COMPUTER) {
					computerSquares.add(new Point(x, y));
				}
			}
		}

		Spaceship1 ss = new Spaceship1(this, this.getNextEntityID(), 8, 2f, 8, JMEAngleFunctions.getYAxisRotation(-1, 0));
		this.actuallyAddEntity(ss);

		ss = new Spaceship1(this, this.getNextEntityID(), 48, 2f, 8, JMEAngleFunctions.getYAxisRotation(-1, 0));
		this.actuallyAddEntity(ss);

		//FlyingSpaceship2 fs = new FlyingSpaceship2(this, this.getNextEntityID(), 8, 5f, 8);
		//this.actuallyAddEntity(fs);

		// Add AI soldiers
		if (Globals.TEST_AI) {
			MA_AISoldier s = new MA_AISoldier(this, this.getNextEntityID(), 0,0,0, (byte)2, false, AbstractAvatar.ANIM_IDLE, "AI TEST");
			this.actuallyAddEntity(s);
			moveAISoldierToStartPosition(s, s.side);
		}

	}


	public void addAISoldier(byte side, String name) {
		MA_AISoldier s = new MA_AISoldier(this, this.getNextEntityID(), 0,0,0, side, false, AbstractAvatar.ANIM_IDLE, name);
		this.actuallyAddEntity(s);
		moveAISoldierToStartPosition(s, s.side);
		//Globals.p("Created AI soldier on side " + side);
	}


	private void moveAISoldierToStartPosition(PhysicalEntity soldier, byte side) {
		float startHeight = .1f;
		List<Point> deploySquares = this.deploySquares[side-1];
		boolean found = false;
		for (int i=0 ; i<20 ; i++) { // only try a certain number of times
			Point p = deploySquares.get(NumberFunctions.rnd(0, deploySquares.size()-1));
			soldier.setWorldTranslation(p.x+0.5f, startHeight, p.y+0.5f);
			if (soldier.simpleRigidBody.checkForCollisions(false).size() == 0) {
				found = true;
				break;
			}
		}
		if (found) {
			//Globals.p(soldier + " starting at " + soldier.getWorldTranslation());
		} else {
			throw new RuntimeException("No space to start!");
		}
	}


	@Override
	protected AbstractServerAvatar createPlayersAvatarEntity(ClientData client, int entityid) {
		SoldierServerAvatar avatar = new SoldierServerAvatar(this, client, client.remoteInput, entityid);
		return avatar;
	}


	@Override
	public void collisionOccurred(SimpleRigidBody<PhysicalEntity> a, SimpleRigidBody<PhysicalEntity> b) {
		PhysicalEntity pa = a.userObject; //pa.getMainNode().getWorldBound();
		PhysicalEntity pb = b.userObject; //pb.getMainNode().getWorldBound();
/*
		if (pa.type != MoonbaseAssaultClientEntityCreator.FLOOR_OR_CEILING && pb.type != MoonbaseAssaultClientEntityCreator.FLOOR_OR_CEILING) {
			//Globals.p("Collision between " + pa + " and " + pb);
		}
*/
		super.collisionOccurred(a, b);

	}


	@Override
	public boolean canCollide(PhysicalEntity a, PhysicalEntity b) {
		return this.collisionValidator.canCollide(a, b);
	}


	@Override
	protected void playerJoinedGame(ClientData client) {
		this.gameNetworkServer.sendMessageToClient(client, new HudDataMessage(this.mapData, this.maGameData.computersDestroyed));
	}


	@Override
	protected Class[] getListofMessageClasses() {
		return new Class[] {HudDataMessage.class, MASimplePlayerData.class};
	}


	@Override
	public byte getSideForPlayer(ClientData client) {
		if (PLAYERS_ARE_ALWAYS_DEFENDERS) {
			return MoonbaseAssaultGlobals.SIDE_DEFENDERS;
		} else {
			byte side = nextPlayerIsDefender ? MoonbaseAssaultGlobals.SIDE_DEFENDERS : MoonbaseAssaultGlobals.SIDE_ATTACKERS;
			nextPlayerIsDefender = !nextPlayerIsDefender;
			return side;
		}
	}


	private HashMap<Byte, Integer> getPlayersPerSide() {
		HashMap<Byte, Integer> map = new HashMap<Byte, Integer>();

		// Load with empty side data
		for (byte side=1 ; side<=2 ; side++) {
			map.put(side,  0);
		}


		for (ClientData client : this.clientList.getClients()) {
			if (client.avatar != null) {
				if (!map.containsKey(client.getSide())) {
					map.put(client.getSide(), 0);
				}
				int val = map.get(client.getSide());
				val++;
				map.put(client.getSide(), val);
			}
		}
		return map;
	}


	@Override
	public boolean doWeHaveSpaces() {
		/*int currentPlayers = 0;
		for(ClientData c : this.clients.values()) {
			if (c.clientStatus == ClientData.ClientStatus.Accepted) {  // only count players actually Accepted!
				currentPlayers++;
			}
		}
		return currentPlayers < MAX_PLAYERS;*/
		return true;
	}


	public List<Point> getComputerSquares() {
		return this.computerSquares;
	}


	public void computerDestroyed(Point p) {
		if (this.computerSquares.contains(p)) {
			super.appendToGameLog("Computer destroyed!");// (" + p.x + "," + p.y + ")");

			this.computerSquares.remove(p);
			this.maGameData.computersDestroyed++;
			this.mapData[p.x][p.y] = MapLoader.DESTROYED_COMPUTER;
			this.sendMessageToInGameClients(new HudDataMessage(this.mapData, this.maGameData.computersDestroyed));

			if (this.maGameData.computersDestroyed >= COMPS_DESTROYED_TO_WIN) {
				winningSide = 1;
				super.gameStatusSystem.setGameStatus(SimpleGameData.ST_FINISHED);
			}
		}
	}


	@Override
	protected byte getWinningSideAtEnd() {
		return this.winningSide;
	}


	@Override
	public int getMinPlayersRequiredForGame() {
		return 1;
	}


	@Override
	public void gameStatusChanged(final int newStatus) {
		super.gameStatusChanged(newStatus);
		this.sendMessageToInGameClients(new HudDataMessage(this.mapData, this.maGameData.computersDestroyed)); // If new game started, show the new map
	}


	@Override
	protected SimplePlayerData createSimplePlayerData() {
		return new MASimplePlayerData();
	}


	// AStar --------------------------------

	@Override
	public int getMapWidth() {
		return this.mapData[0].length;
	}


	@Override
	public int getMapHeight() {
		return this.mapData.length;
	}


	@Override
	public boolean isMapSquareTraversable(int x, int z) {
		return this.mapData[x][z] != MapLoader.WALL && this.mapData[x][z] != MapLoader.COMPUTER && this.mapData[x][z] != MapLoader.DESTROYED_COMPUTER;
	}


	@Override
	public float getMapSquareDifficulty(int x, int z) {
		return 1;
	}

	//--------------------------------


}
