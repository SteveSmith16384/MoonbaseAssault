package com.scs.moonbaseassault.server;

import java.awt.Point;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.jme3.system.JmeContext;
import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.moonbaseassault.entities.FlyingSpaceship2;
import com.scs.moonbaseassault.entities.MA_AISoldier;
import com.scs.moonbaseassault.entities.SoldierServerAvatar;
import com.scs.moonbaseassault.entities.Spaceship1;
import com.scs.moonbaseassault.netmessages.HudDataMessage;
import com.scs.moonbaseassault.shared.MoonbaseAssaultCollisionValidator;
import com.scs.moonbaseassault.shared.MoonbaseAssaultGameData;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.data.GameOptions;
import com.scs.stevetech1.data.SimpleGameData;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.jme.JMEAngleFunctions;
import com.scs.stevetech1.netmessages.GeneralCommandMessage;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.server.Globals;

import ssmith.astar.IAStarMapInterface;
import ssmith.lang.Functions;
import ssmith.lang.NumberFunctions;
import ssmith.util.MyProperties;

public class MoonbaseAssaultServer extends AbstractGameServer implements IAStarMapInterface {

	private static final int COMPS_DESTROYED_TO_WIN = 10;
	public static final boolean PLAYERS_ARE_DEFENDERS = true;

	public static final String GAME_ID = "Moonbase Assault";

	// Sides
	public static final int SIDE_ATTACKER = 1;
	public static final int SIDE_DEFENDER = 2;

	public static final float CEILING_HEIGHT = 1.4f;
	public static final float LASER_DIAM = 0.02f;

	private int mapData[][]; // Also used to tell the client what the scanner should show
	private List<Point> computerSquares; // For A*
	public ArrayList<Point>[] deploySquares;
	private MoonbaseAssaultCollisionValidator collisionValidator = new MoonbaseAssaultCollisionValidator();
	private int winningSide = 2; // Defenders win by default
	private CreateUnitsSystem createUnitsSystem;
	private MoonbaseAssaultGameData maGameData;

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
			int gamePort = props.getPropertyAsInt("gamePort", 6145);
			//String lobbyIpAddress = null;//props.getPropertyAsString("lobbyIpAddress", "localhost");
			//int lobbyPort = props.getPropertyAsInt("lobbyPort", 6146);

			int tickrateMillis = props.getPropertyAsInt("tickrateMillis", 25);
			int sendUpdateIntervalMillis = props.getPropertyAsInt("sendUpdateIntervalMillis", 40);
			int clientRenderDelayMillis = props.getPropertyAsInt("clientRenderDelayMillis", 200);
			int timeoutMillis = props.getPropertyAsInt("timeoutMillis", 100000);

			//startLobbyServer(lobbyPort, timeoutMillis); // Start the lobby in the same process, why not?  Feel from to comment this line out and run it seperately (If you want a lobby).

			new MoonbaseAssaultServer(gameIpAddress, gamePort, //lobbyIpAddress, lobbyPort, 
					tickrateMillis, sendUpdateIntervalMillis, clientRenderDelayMillis, timeoutMillis);//, gravity, aerodynamicness);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	private static void startLobbyServer(int lobbyPort, int timeout) {
		Thread r = new Thread("LobbyServer") {

			@Override
			public void run() {
				try {
					new MoonbaseAssaultLobbyServer(lobbyPort, timeout);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		r.start();


	}
	 */

	private MoonbaseAssaultServer(String gameIpAddress, int gamePort, //String lobbyIpAddress, int lobbyPort, 
			int tickrateMillis, int sendUpdateIntervalMillis, int clientRenderDelayMillis, int timeoutMillis) throws IOException {
		super(GAME_ID, "key", new GameOptions(5*1000, 10*60*1000, 10*1000, 
				gameIpAddress, gamePort, //lobbyIpAddress, lobbyPort, 
				10, 5), tickrateMillis, sendUpdateIntervalMillis, clientRenderDelayMillis, timeoutMillis);

		start(JmeContext.Type.Headless);
	}


	@Override
	public void simpleInitApp() {
		try {
			//String text = new String(Files.readAllBytes(Paths.get(getClass().getResource("/serverdata/ai_names.txt").toURI())));
			String text = Functions.readAllFileFromJar(this.getClass().getClassLoader(), "serverdata/ai_names.txt");
			String[] lines = text.split("\n");
			createUnitsSystem = new CreateUnitsSystem(this, lines);
		} catch (Exception e) {
			throw new RuntimeException("Error loading names", e);
		}

		super.simpleInitApp();
	}


	@Override
	public void simpleUpdate(float tpf_secs) {
		super.simpleUpdate(tpf_secs); // this.maGameData

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
		this.gameNetworkServer.sendMessageToAll(new HudDataMessage(this.mapData, this.maGameData.computersDestroyed));

		MapLoader map = new MapLoader(this);
		try {
			//map.loadMap("/serverdata/moonbaseassault_small.csv");
			map.loadMap("/serverdata/moonbaseassault.csv");
			mapData = map.scannerData;
			this.deploySquares = map.deploySquares;

			this.computerSquares = new ArrayList<Point>();
			for (int y=0 ; y<mapData.length ; y++) {
				for (int x=0 ; x<mapData.length ; x++) {
					if (this.mapData[x][y] == MapLoader.COMPUTER) {
						computerSquares.add(new Point(x, y));
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

		if (!PLAYERS_ARE_DEFENDERS) {
			Spaceship1 ss = new Spaceship1(this, this.getNextEntityID(), 8, 2f, 8, JMEAngleFunctions.getYAxisRotation(-1, 0));
			this.actuallyAddEntity(ss);

			ss = new Spaceship1(this, this.getNextEntityID(), 48, 2f, 8, JMEAngleFunctions.getYAxisRotation(-1, 0));
			this.actuallyAddEntity(ss);

			FlyingSpaceship2 fs = new FlyingSpaceship2(this, this.getNextEntityID(), 8, 5f, 8);
			this.actuallyAddEntity(fs);
		}

		// Add AI soldiers
		if (Globals.TEST_AI) {
			MA_AISoldier s = new MA_AISoldier(this, this.getNextEntityID(), 0,0,0, 2, AbstractAvatar.ANIM_IDLE, "AI TEST");
			this.actuallyAddEntity(s);
			moveAISoldierToStartPosition(s, s.side);
		}

	}


	public void addAISoldier(int side, String name) {
		//String name = (side == 1 ? "Attacker" : "Defender") + " " + num;
		MA_AISoldier s = new MA_AISoldier(this, this.getNextEntityID(), 0,0,0, side, AbstractAvatar.ANIM_IDLE, name);
		this.actuallyAddEntity(s);
		moveAISoldierToStartPosition(s, s.side);
		Globals.p("Created AI soldier on side " + side);
	}


	private void moveAISoldierToStartPosition(PhysicalEntity soldier, int side) {
		float startHeight = .1f;
		//if (!Globals.TEST_AI) {
		List<Point> deploySquares = this.deploySquares[side-1];
		boolean found = false;
		for (int i=0 ; i<20 ; i++) { // only try a certain number of times
			Point p = deploySquares.get(NumberFunctions.rnd(0, deploySquares.size()-1));
			soldier.setWorldTranslation(p.x+0.5f, startHeight, p.y+0.5f);
			if (soldier.simpleRigidBody.checkForCollisions().size() == 0) {
				found = true;
				break;
			}
		}
		if (found) {
			Globals.p(soldier + " starting at " + soldier.getWorldTranslation());
		} else {
			throw new RuntimeException("No space to start!");
		}
		/*} else {
			soldier.setWorldTranslation(1.5f, startHeight, 1.5f);
		}*/
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

		if (pa.type != MoonbaseAssaultClientEntityCreator.FLOOR && pb.type != MoonbaseAssaultClientEntityCreator.FLOOR) {
			//Globals.p("Collision between " + pa + " and " + pb);
		}

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
		return new Class[] {HudDataMessage.class};
	}


	@Override
	public int getSide(ClientData client) {
		if (PLAYERS_ARE_DEFENDERS) {
			return SIDE_DEFENDER;
		} else {
			return SIDE_ATTACKER;
		}
	}


	private HashMap<Integer, Integer> getPlayersPerSide() {
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();

		// Load with empty side data
		for (int side=1 ; side<=2 ; side++) {
			map.put(side,  0);
		}


		for (ClientData client : this.clients.values()) {
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
			super.appendToGameLog("Computer destroyed!  At " + p.x + "," + p.y);

			this.computerSquares.remove(p);
			this.maGameData.computersDestroyed++;
			this.mapData[p.x][p.y] = MapLoader.DESTROYED_COMPUTER;
			this.gameNetworkServer.sendMessageToAll(new HudDataMessage(this.mapData, this.maGameData.computersDestroyed));

			if (this.maGameData.computersDestroyed >= COMPS_DESTROYED_TO_WIN) {
				winningSide = 1;
				super.gameStatusSystem.setGameStatus(SimpleGameData.ST_FINISHED);
			}
		}
	}


	@Override
	protected int getWinningSideAtEnd() {
		return this.winningSide;
	}


	@Override
	public int getMinPlayersRequiredForGame() {
		return 1;
	}


	@Override
	public void gameStatusChanged(int newStatus) {
		super.gameStatusChanged(newStatus);
		this.gameNetworkServer.sendMessageToAll(new HudDataMessage(this.mapData, this.maGameData.computersDestroyed)); // If new game started, show the new map
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


	@Override
	protected String getSideName(int side) {
		switch (side) {
		case 1: return "The Attackers";
		case 2: return "The Defenders";
		default: return "Unknown";
		}
	}

}
