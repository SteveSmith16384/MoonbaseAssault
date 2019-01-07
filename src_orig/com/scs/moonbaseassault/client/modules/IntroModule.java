package com.scs.moonbaseassault.client.modules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.scs.moonbaseassault.MoonbaseAssaultGlobals;
import com.scs.moonbaseassault.client.MoonbaseAssaultClient;
import com.scs.moonbaseassault.client.intro.SimpleMoonbaseWall;
import com.scs.moonbaseassault.models.SoldierModel;
import com.scs.moonbaseassault.server.MapLoader;
import com.scs.stevetech1.server.Globals;

import ssmith.lang.Functions;
import ssmith.lang.NumberFunctions;

/*
 * Module order:-
 * 1 - Intro Module
 * 2 - JonlanIntro
 * 3 - ConnectModule - connect to server
 * 4 - Pre-game Module
 * 5 - Main Module - actually join game!
 * 6 - Disconnected module
 * 
 */
public class IntroModule extends AbstractModule {

	private static final int STAGE_TITLE = 0;
	private static final int STAGE_EXPLODE_TITLE = 2;

	public static ColorRGBA defaultColour = ColorRGBA.Green;

	private static final Vector3f vDown = new Vector3f(0, -800f, 0);
	private static final int HANDLED = MapLoader.HANDLED;
	private static int WALL = MapLoader.WALL;

	private int mapCode[][];
	private int mapSize;
	private Node introNode;
	private List<Node> walls;
	private Vector3f camPos, camStartPos, camEndPos;
	private float moveFrac = 0;
	private Node currentNode = null;
	private float waitFor = 0;
	private int currentStage = STAGE_TITLE;
	private float explodeDuration = 6;

	public IntroModule(MoonbaseAssaultClient client) {
		super(client);

	}


	@Override
	public void simpleInit() {
		BitmapFont fontSmall = client.getAssetManager().loadFont("Interface/Fonts/Console.fnt");

		BitmapText bmpText = new BitmapText(fontSmall, false);
		bmpText.setColor(defaultColour);
		bmpText.setLocalTranslation(10, 10, 0);
		client.getGuiNode().attachChild(bmpText);
		bmpText.setText("Click mouse to Start");

		introNode = new Node("IntroNode");

		SimpleMoonbaseWall floor = new SimpleMoonbaseWall(client, -50, -1, -50, 100, 1f, 100f, "Textures/moonrock.png");
		this.introNode.attachChild(floor);

		try {
			loadMap("serverdata/intro_map.csv");
		} catch (IOException e) {
			throw new RuntimeException("Unable to load map", e);
		}

		camStartPos = new Vector3f(0, 3, mapSize/2);
		camPos = new Vector3f();
		camEndPos = new Vector3f(mapSize/2, mapSize, (mapSize/2)+1);

		this.client.getCamera().setLocation(camStartPos);
		this.client.getCamera().lookAt(new Vector3f(mapSize/2, 0, mapSize/2), Vector3f.UNIT_Y);

		this.client.getRootNode().attachChild(introNode);

		// Lights
		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White.mult(1f));
		introNode.addLight(al);

		DirectionalLight sun = new DirectionalLight();
		sun.setColor(ColorRGBA.White);
		sun.setDirection(new Vector3f(.4f, -.8f, .4f).normalizeLocal());
		introNode.addLight(sun);

		// Add shadows
		final int SHADOWMAP_SIZE = 512;
		DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(client.getAssetManager(), SHADOWMAP_SIZE, 2);
		dlsr.setLight(sun);
		client.getViewPort().addProcessor(dlsr);

		if (Globals.DEBUG_3D_PROBLEM) {
			SoldierModel m = new SoldierModel(client.getAssetManager(), false, (byte)0, false);
			client.getRootNode().attachChild(m.createAndGetModel());
		}
	}


	@Override
	public void simpleUpdate(float tpfSecs) {
		super.simpleUpdate(tpfSecs);

		if (tpfSecs > 1) {
			tpfSecs = 1;
		}

		if (waitFor > 0 ) {
			waitFor -= tpfSecs;
			return;
		}

		switch (this.currentStage) {
		case STAGE_TITLE:
			if (walls.size() > 0) {
				if (currentNode == null) { //this.client.getRootNode().getChild(1).getChild(2).getWorldTranslation();
					currentNode = walls.remove(NumberFunctions.rnd(0, walls.size()-1));
					this.introNode.attachChild(currentNode);
					currentNode.getLocalTranslation().y = 10;
					if (walls.isEmpty()) {
						camStartPos = client.getCamera().getLocation().clone();
					}
				}
				// Move current wall 
				float speed = (walls.size()/2)+1;
				currentNode.setLocalTranslation(currentNode.getLocalTranslation().add(vDown.mult(tpfSecs / speed )));
				if (currentNode.getLocalTranslation().y <= 0) {
					currentNode.getLocalTranslation().y = 0;
					currentNode = null;
				}

				// Move cameras back
				if (!Globals.DEBUG_3D_PROBLEM) {
					this.client.getCamera().getLocation().z -= tpfSecs;
					this.client.getCamera().lookAt(new Vector3f(mapSize/2, 0, mapSize/2), Vector3f.UNIT_Y);
				}
			} else {
				// Start moving cam
				this.moveFrac += tpfSecs;
				if (moveFrac <= 1) {
					camPos.interpolateLocal(camStartPos, camEndPos, moveFrac);
					this.client.getCamera().setLocation(camPos);
					this.client.getCamera().lookAt(new Vector3f(mapSize/2, 0, mapSize/2), Vector3f.UNIT_Y);
				} else {
					this.waitFor = 2;
					this.currentStage = STAGE_EXPLODE_TITLE;
					for(Spatial node : this.introNode.getChildren()) {
						if (node instanceof SimpleMoonbaseWall) {
							SimpleMoonbaseWall smw = (SimpleMoonbaseWall)node;
							float x = NumberFunctions.rndFloat(-1, 1);
							float y = NumberFunctions.rndFloat(-1, 5);
							float z = NumberFunctions.rndFloat(-1, 1);
							smw.setUserData("offset", new Vector3f(x, y, z).normalizeLocal());
						}
					}
				}
			}
			break;

		case STAGE_EXPLODE_TITLE:
			explodeDuration -= tpfSecs;
			for(Spatial node : this.introNode.getChildren()) {
				if (node instanceof SimpleMoonbaseWall) {
					SimpleMoonbaseWall smw = (SimpleMoonbaseWall)node;
					Vector3f offset = smw.getUserData("offset");
					smw.setLocalTranslation(smw.getLocalTranslation().add(offset.mult(tpfSecs*10)));
					//smw.rot
				}
			}
			if (explodeDuration <= 0) {
				client.startJonlanModule();
			}
			break;
		}
	}


	@Override
	public void mouseClicked() {
		client.startConnectToServerModule();
	}


	@Override
	public void destroy() {
		this.introNode.removeFromParent();
		client.getGuiNode().detachAllChildren();
	}


	public void loadMap(String s) throws IOException {
		String text = Functions.readAllTextFileFromJar(s);
		String[] lines = text.split("\n");

		walls = new ArrayList<Node>();

		mapSize = Integer.parseInt(lines[0].split(",")[0]);
		mapCode = new int[mapSize][mapSize];

		for (int lineNum=1 ; lineNum<lines.length ; lineNum++) { // Skip line 1
			String line = lines[lineNum];
			String[] tokens = line.split(",");
			for (int x=0 ; x<tokens.length ; x++) {
				String cell = tokens[x];
				mapCode[x][lineNum-1] = Integer.parseInt(cell);
			}
		}

		/*
		// print map
		for (int y=0 ; y<mapsize ; y++) {
			for (int x=0 ; x<mapsize ; x++) {
				if (mapCode[x][y] == WALL) {
					System.out.print("X");
				} else if (mapCode[x][y] == INT_FLOOR) {
					System.out.print(".");
				} else if (mapCode[x][y] == EXT_FLOOR) {
					System.out.print(":");
				} else if (mapCode[x][y] == COMPUTER) {
					System.out.print("C");
				} else if (mapCode[x][y] == DOOR_LR) {
					System.out.print("L");
				} else if (mapCode[x][y] == DOOR_UD) {
					System.out.print("U");
				} else {
					System.out.print(" ");
				}					
			}
			System.out.println("");
		}
		 */

		// Generate map!
		{
			int y = 0;
			while (y < mapSize) {
				int x = 0;
				while (x < mapSize-1) {
					if (mapCode[x][y] == WALL && mapCode[x+1][y] == WALL) {
						checkForHorizontalWalls(x, y);
					}				
					x++;
				}						
				y++;
			}
		}

		{
			// Vertical walls
			int y = 0;
			while (y < mapSize-1) {
				int x = 0;
				while (x < mapSize) {
					if (mapCode[x][y] == WALL) {// && handled[x][y+1] == WALL) {
						checkForVerticalWalls(x, y);
					}				
					x++;
				}						
				y++;
			}
		}

	}


	private void checkForHorizontalWalls(int sx, int sy) {
		int x;
		for (x=sx ; x<mapSize ; x++) {
			if (mapCode[x][sy] != WALL) {
				break;
			}
			mapCode[x][sy] = HANDLED;
		}
		x--;
		float width = x-sx+1;
		SimpleMoonbaseWall wall = new SimpleMoonbaseWall(client, sx, 0f, sy, width, MoonbaseAssaultGlobals.CEILING_HEIGHT, 1, "Textures/ufo2_03.png");
		walls.add(wall);
	}


	private void checkForVerticalWalls(int sx, int sy) {
		int y;
		for (y=sy ; y<mapSize ; y++) {
			if (mapCode[sx][y] != WALL) {
				break;
			}
			mapCode[sx][y] = HANDLED;
		}
		y--;
		SimpleMoonbaseWall wall = new SimpleMoonbaseWall(client, sx, 0f, sy, 1, MoonbaseAssaultGlobals.CEILING_HEIGHT, y-sy+1, "Textures/ufo2_03.png");
		walls.add(wall);
	}


}
