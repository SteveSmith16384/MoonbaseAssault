package com.scs.moonbaseassault.client.modules;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.jme3.light.AmbientLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.scs.moonbaseassault.client.MoonbaseAssaultClient;
import com.scs.moonbaseassault.entities.MoonbaseWall;
import com.scs.moonbaseassault.server.MapLoader;
import com.scs.moonbaseassault.server.MoonbaseAssaultServer;
import com.scs.stevetech1.data.SimpleGameData;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.input.SimpleMouseInput;

import ssmith.lang.Functions;
import ssmith.lang.NumberFunctions;

public class IntroModule extends AbstractModule { // todo - create SimpleMoonbaseWall just for intro

	private static final Vector3f vDown = new Vector3f(0, -800f, 0);
	private static final int HANDLED = MapLoader.HANDLED;
	private static int WALL = MapLoader.WALL;

	private int mapCode[][];
	private int mapSize;
	private Node introNode;
	private List<PhysicalEntity> walls;
	private Vector3f camPos, camStartPos, camEndPos;
	private float pcent = 0;
	private PhysicalEntity current = null;

	public IntroModule(MoonbaseAssaultClient client) {
		super(client);
	}


	@Override
	public void simpleInit() {
		this.client.gameData = new SimpleGameData(); // Need to create wall entities for intro

		try {
			loadMap("serverdata/intro_map.csv");
		} catch (Exception e) {
			e.printStackTrace();
		}

		camStartPos = new Vector3f(mapSize/2, 5, -mapSize);
		camPos = new Vector3f();
		camEndPos = new Vector3f(mapSize/2, mapSize, mapSize/2);

		this.client.getCamera().setLocation(camStartPos);
		this.client.getCamera().lookAt(new Vector3f(mapSize/2, 0, mapSize/2), Vector3f.UNIT_Y);

		introNode = new Node("IntroNode");
		this.client.getRootNode().attachChild(introNode);

		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White.mult(1f));
		introNode.addLight(al);

		client.input = new SimpleMouseInput(client.getInputManager(), 1f);
	}


	@Override
	public void simpleUpdate(float tpfSecs) {
		if (walls.size() > 0) {
			if (current == null) {
				current = walls.remove(NumberFunctions.rnd(0, walls.size()-1));
				current.getWorldTranslation().y = 10;
				this.introNode.attachChild(current.getMainNode());
				if (walls.isEmpty()) {
					camStartPos = client.getCamera().getLocation().clone();
				}
			}
			current.adjustWorldTranslation(vDown.mult(tpfSecs / (walls.size()+1) ));
			if (current.getWorldTranslation().y <= 0) {
				current.getWorldTranslation().y = 0;
				current = null;
			}
			this.client.getCamera().getLocation().z += tpfSecs;
		} else {
			// Start moving cam
			this.pcent += tpfSecs;
			if (pcent <= 1) {
				//camOrigStartPos = this.camStartPos.clone();
				camPos.interpolateLocal(camStartPos, camEndPos, pcent);
				this.client.getCamera().setLocation(camPos);
			}
		}

		this.client.getCamera().lookAt(new Vector3f(mapSize/2, 0, mapSize/2), Vector3f.UNIT_Y);

		if (this.client.input.isAbilityPressed(1)) {
			client.setMainModule();
		}
	}


	public void loadMap(String s) throws FileNotFoundException, IOException, URISyntaxException {
		String text = Functions.readAllFileFromJar(s);
		String[] lines = text.split("\n");

		walls = new ArrayList<PhysicalEntity>();

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
		MoonbaseWall wall = new MoonbaseWall(client, client.getNextEntityID(), sx, 0f, sy, width, MoonbaseAssaultServer.CEILING_HEIGHT, 1, "Textures/ufo2_03.png");
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
		MoonbaseWall wall = new MoonbaseWall(client, client.getNextEntityID(), sx, 0f, sy, 1, MoonbaseAssaultServer.CEILING_HEIGHT, y-sy+1, "Textures/ufo2_03.png");
		walls.add(wall);
	}


	@Override
	public void destroy() {
		this.introNode.removeFromParent();
		// todo - remove entities
	}


}
