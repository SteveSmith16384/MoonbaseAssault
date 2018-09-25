package com.scs.moonbaseassault.server;

import java.awt.Point;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import com.jme3.math.Vector3f;
import com.scs.moonbaseassault.MATextures;
import com.scs.moonbaseassault.entities.Computer;
import com.scs.moonbaseassault.entities.DestroyedComputer;
import com.scs.moonbaseassault.entities.FloorOrCeiling;
import com.scs.moonbaseassault.entities.GasCannister;
import com.scs.moonbaseassault.entities.GenericFloorTex;
import com.scs.moonbaseassault.entities.MapBorder;
import com.scs.moonbaseassault.entities.MediPack;
import com.scs.moonbaseassault.entities.MoonbaseWall;
import com.scs.moonbaseassault.entities.SlidingDoor;
import com.scs.moonbaseassault.entities.SpaceCrate;
import com.scs.moonbaseassault.entities.VidScreen;
import com.scs.stevetech1.jme.JMEAngleFunctions;
import com.scs.stevetech1.server.Globals;

import ssmith.lang.Functions;
import ssmith.lang.NumberFunctions;

public class MapLoader {

	public static final int HANDLED = 0;
	public static final int INT_FLOOR = 1;
	public static final int EXT_FLOOR = 2;
	public static final int WALL = 3;
	public static final int DOOR_LR = 4;
	public static final int DOOR_UD = 5;
	public static final int COMPUTER = 6;
	public static final int DESTROYED_COMPUTER = 7;

	private static final float INT_FLOOR_HEIGHT = 0.05f;

	private int mapCode[][];
	private int mapsize;
	private int totalWalls, totalFloors, totalCeilings, totalDoors;
	private MoonbaseAssaultServer moonbaseAssaultServer;
	public int scannerData[][];
	public ArrayList<Point>[] deploySquares;
	public ArrayList<Point> floorSquares;

	public MapLoader(MoonbaseAssaultServer _moonbaseAssaultServer) {
		super();

		moonbaseAssaultServer = _moonbaseAssaultServer;
		deploySquares = new ArrayList[2];
		this.deploySquares[0] = new ArrayList<Point>();
		this.deploySquares[1] = new ArrayList<Point>();
	}


	public void loadMap(String s) throws FileNotFoundException, IOException, URISyntaxException {
		String text = Functions.readAllFileFromJar(s);
		String[] lines = text.split("\n");

		mapsize = Integer.parseInt(lines[0].split(",")[0]);
		mapCode = new int[mapsize][mapsize];

		for (int lineNum=1 ; lineNum<lines.length ; lineNum++) { // Skip line 1
			String line = lines[lineNum];
			String[] tokens = line.split(",");
			for (int x=0 ; x<tokens.length ; x++) {
				String cell = tokens[x];
				String[] subtokens = cell.split("\\|");  // FLOOR:1|DEPLOY:2|
				for(String part : subtokens) {
					String stringAndCode[] = part.split(":");
					if (stringAndCode[0].equals("WALL")) {
						mapCode[x][lineNum-1] = WALL;
					} else if (stringAndCode[0].equals("COMP")) {
						mapCode[x][lineNum-1] = COMPUTER;
					} else if (stringAndCode[0].equals("DOOR")) {
						if (stringAndCode[1].equals("1")) {
							mapCode[x][lineNum-1] = DOOR_UD;
						} else if (stringAndCode[1].equals("2")) {
							mapCode[x][lineNum-1] = DOOR_LR;
						}
					} else if (stringAndCode[0].equals("FLOOR")) {
						if (stringAndCode[1].equals("2")) {
							mapCode[x][lineNum-1] = EXT_FLOOR;
						} else {
							mapCode[x][lineNum-1] = INT_FLOOR;
						}
					} else if (stringAndCode[0].equals("DEPLOY")) {
						if (stringAndCode[1].equals("1")) {
							this.deploySquares[0].add(new Point(x, lineNum-1));
						} else {
							this.deploySquares[1].add(new Point(x, lineNum-1));
						}
					}					
				}
			}
		}

		// Copy for scanner data
		scannerData = new int[mapsize][mapsize];
		for (int y=0 ; y<mapsize ; y++) {
			for (int x=0 ; x<mapsize ; x++) {
				scannerData[x][y] = mapCode[x][y];
			}
		}


		//printMap();

		// Generate map!
		totalWalls = 0;
		{
			int y = 0;
			while (y < mapsize) {
				int x = 0;
				while (x < mapsize-1) {
					if (mapCode[x][y] == WALL && mapCode[x+1][y] == WALL) {
						checkForHorizontalWalls(x, y);
					}				
					x++;
				}						
				y++;
			}
		}

		//printMap();

		{
			// Vertical walls
			int y = 0;
			while (y < mapsize-1) {
				int x = 0;
				while (x < mapsize) {
					if (mapCode[x][y] == WALL) {
						checkForVerticalWalls(x, y);
					}				
					x++;
				}						
				y++;
			}
		}

		//printMap();

		// Doors && comps
		{
			for (int y=0 ; y<mapsize ; y++) {
				for (int x=0 ; x<mapsize ; x++) {
					if (mapCode[x][y] == DOOR_LR) {
						SlidingDoor door = new SlidingDoor(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), x, 0, y, 1, MoonbaseAssaultServer.CEILING_HEIGHT, MATextures.DOOR_LR, 0);
						moonbaseAssaultServer.actuallyAddEntity(door);
						mapCode[x][y] = INT_FLOOR; // So we create a floor below it
						this.totalDoors++;

						GenericFloorTex gft = new GenericFloorTex(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), x-.5f, INT_FLOOR_HEIGHT + 0.01f, y+.5f, 1f, 1f, "Textures/floor4.jpg");
						moonbaseAssaultServer.actuallyAddEntity(gft);

					} else if (mapCode[x][y] == DOOR_UD) {
						SlidingDoor door = new SlidingDoor(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), x, 0, y, 1, MoonbaseAssaultServer.CEILING_HEIGHT, MATextures.DOOR_LR, 270);
						moonbaseAssaultServer.actuallyAddEntity(door);
						mapCode[x][y] = INT_FLOOR; // So we create a floor below it
						this.totalDoors++;

						GenericFloorTex gft = new GenericFloorTex(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), x-.5f, INT_FLOOR_HEIGHT + 0.01f, y+.5f, 1f, 1f, "Textures/floor4.jpg");
						moonbaseAssaultServer.actuallyAddEntity(gft);

					} else if (mapCode[x][y] == COMPUTER) {
						Computer comp = new Computer(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), x, 0, y, x, y);
						moonbaseAssaultServer.actuallyAddEntity(comp);
						mapCode[x][y] = INT_FLOOR; // So we create a floor below it
					} else if (mapCode[x][y] == DESTROYED_COMPUTER) {

						DestroyedComputer comp = new DestroyedComputer(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), x, 0, y);
						moonbaseAssaultServer.actuallyAddEntity(comp);
						mapCode[x][y] = DESTROYED_COMPUTER; // So we create a floor below it
					}
				}
			}
		}

		this.totalFloors = 0;
		this.totalCeilings = 0;

		//this.printMap();
		doInteriorFloorsAndCeilings(true, false); // Across first, ignore 1x1
		//this.printMap();
		doInteriorFloorsAndCeilings(false, false); // down first, ignore 1x1
		//this.printMap();
		doInteriorFloorsAndCeilings(true, true); // across again, filling in 1x1
		//this.printMap();

		// One big moon floor
		FloorOrCeiling moonrock = new FloorOrCeiling(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), "Big Ext Floor", 0, 0, 0, mapsize, .5f, mapsize, MATextures.MOONROCK, true);
		moonbaseAssaultServer.actuallyAddEntity(moonrock);

		// Border
		MapBorder borderL = new MapBorder(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), 0, 0, 0, mapsize, Vector3f.UNIT_Z);
		moonbaseAssaultServer.actuallyAddEntity(borderL);
		MapBorder borderR = new MapBorder(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), mapsize+MapBorder.BORDER_WIDTH, 0, 0, mapsize, Vector3f.UNIT_Z);
		moonbaseAssaultServer.actuallyAddEntity(borderR);
		MapBorder borderBack = new MapBorder(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), 0, 0, mapsize, mapsize, Vector3f.UNIT_X);
		moonbaseAssaultServer.actuallyAddEntity(borderBack);
		MapBorder borderFront = new MapBorder(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), 0, 0, -MapBorder.BORDER_WIDTH, mapsize, Vector3f.UNIT_X);
		moonbaseAssaultServer.actuallyAddEntity(borderFront);

		Globals.p("Finished creating map.  Created " + this.totalWalls + " walls, " + this.totalFloors + " floors, " + this.totalCeilings + " ceilings, " + totalDoors + " doors.");

		Vector3f down = new Vector3f(0, -1, 0);

		// Scenery
		int numGasCannisters = Math.min(30, floorSquares.size()/2); 
		for (int i=0 ; i<numGasCannisters ; i++) {
			Point p = this.floorSquares.remove(NumberFunctions.rnd(0,  floorSquares.size()-1));
			GasCannister gas = new GasCannister(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), p.x+0.5f, INT_FLOOR_HEIGHT + 0.1f, p.y+0.5f);
			moonbaseAssaultServer.actuallyAddEntity(gas);
			Vector3f dir = JMEAngleFunctions.getRandomDirection_4();
			moonbaseAssaultServer.moveEntityUntilItHitsSomething(gas, dir);
			Vector3f dir2 = JMEAngleFunctions.turnRight(dir);
			moonbaseAssaultServer.moveEntityUntilItHitsSomething(gas, dir2);
			moonbaseAssaultServer.moveEntityUntilItHitsSomething(gas, down, 0.1f);
			//Globals.p("Gas can at " + gas.getWorldTranslation());
		}

		int numCrates = Math.min(30, floorSquares.size()/4);
		for (int i=0 ; i<numCrates ; i++) {
			Point p = this.floorSquares.remove(NumberFunctions.rnd(0,  floorSquares.size()-1));
			float size = .2f; //Must be thin enough for them to be able to move past each other when positioning //  NumberFunctions.rndFloat(.2f, .3f);
			int rot = NumberFunctions.rnd(0,  90);
			SpaceCrate spacecrate = new SpaceCrate(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), p.x+.5f, size+0.1f, p.y+.5f, size, size, size, MATextures.SPACECRATE1, rot);
			moonbaseAssaultServer.actuallyAddEntity(spacecrate);
			Vector3f dir = JMEAngleFunctions.getRandomDirection_4();
			moonbaseAssaultServer.moveEntityUntilItHitsSomething(spacecrate, dir);
			Vector3f dir2 = JMEAngleFunctions.turnRight(dir);
			moonbaseAssaultServer.moveEntityUntilItHitsSomething(spacecrate, dir2);
			moonbaseAssaultServer.moveEntityUntilItHitsSomething(spacecrate, down, 0.1f);
			//Globals.p("SpaceCrate at " + spacecrate.getWorldTranslation());
		}
	}


	private void checkForHorizontalWalls(int sx, int sy) {
		int x;
		for (x=sx ; x<mapsize ; x++) {
			if (mapCode[x][sy] != WALL) {
				break;
			}
			mapCode[x][sy] = HANDLED;
		}
		x--;
		//Globals.p("Creating wall at " + sx + ", " + sy + " length: " + (x-sx));
		float width = x-sx+1;
		MoonbaseWall wall = new MoonbaseWall(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), 
				sx, 0f, sy, 
				width, MoonbaseAssaultServer.CEILING_HEIGHT, 1, 
				MATextures.MOONBASE_WALL);
		moonbaseAssaultServer.actuallyAddEntity(wall);
		totalWalls++;

		/*
		if (width > 3 && NumberFunctions.rnd(1, 2) == 1) {
			// Create offset wall - todo - make non collidabe?
			float extra = 0.15f;
			float newWidth = width/4;
			MoonbaseWall wall2 = new MoonbaseWall(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), 
					sx+((width-newWidth)/2), 0.2f, sy-extra, 
					newWidth, MoonbaseAssaultServer.CEILING_HEIGHT-.4f, 1+(extra*2), 
					MATextures.MOONBASE_WALL);
			moonbaseAssaultServer.actuallyAddEntity(wall2);
		}
		 */

		if (width > 3) {
			if (NumberFunctions.rnd(1, 2) == 1) {
				// Create lights
				for (int i=0 ; i<width ; i+=2) {
					float w_h = 0.25f;
					float d = 0.05f;
					MoonbaseWall wall2 = new MoonbaseWall(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), 
							sx+0.5f+i, 0.8f, sy-d, 
							w_h, w_h, 1+(d*2), 
							MATextures.WALL_LIGHT);
					moonbaseAssaultServer.actuallyAddEntity(wall2);
				}
			} else {
				// Vid screen
				float w_h = 0.75f;
				float d = 0.01f;
				VidScreen wall2 = new VidScreen(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), 
						sx+(width/2), 0.5f, sy-d, 
						w_h, w_h, 1+(d*2), 
						MATextures.JETPAC_VID);
				moonbaseAssaultServer.actuallyAddEntity(wall2);
			}
		}

	}


	private void checkForVerticalWalls(int sx, int sy) {
		int y;
		for (y=sy ; y<mapsize ; y++) {
			if (mapCode[sx][y] != WALL) {
				break;
			}
			mapCode[sx][y] = HANDLED;
		}
		y--;
		//Globals.p("Creating wall at " + sx + ", " + sy + " length: " + (y-sy));
		MoonbaseWall wall = new MoonbaseWall(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), sx, 0f, sy, 1, MoonbaseAssaultServer.CEILING_HEIGHT, y-sy+1, MATextures.MOONBASE_WALL);
		moonbaseAssaultServer.actuallyAddEntity(wall);
		totalWalls++;
	}


	private void doInteriorFloorsAndCeilings(boolean acrossFirst, boolean do1x1) {
		floorSquares = new ArrayList<Point>();
		for (int y=0 ; y<mapsize ; y++) {
			for (int x=0 ; x<mapsize ; x++) {
				if (mapCode[x][y] == INT_FLOOR) {
					interiorFloorAndCeiling(x, y, acrossFirst, do1x1);
					//this.printMap();
				}
			}
		}
	}


	private void interiorFloorAndCeiling(int sx, int sy, boolean acrossFirst, boolean do1x1) {
		Point p = null;
		if (acrossFirst) {
			p = this.acrossThenDown(sx, sy);
		} else {
			p = this.downThenAcross(sx, sy);
		}
		int ex = p.x;
		int ey = p.y;

		int w = ex-sx;
		int d = ey-sy;

		if (!do1x1) {
			//if (w == 1 && d == 1) {
			if (w <= 2 && d <= 2) {
				return;
			}
		}

		FloorOrCeiling floor = new FloorOrCeiling(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), "Int floor", sx, INT_FLOOR_HEIGHT, sy, w, .5f, d, MATextures.ESCAPE_HATCH, true);//"Textures/escape_hatch.jpg");
		moonbaseAssaultServer.actuallyAddEntity(floor);
		this.totalFloors++;

		MediPack medi = new MediPack(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), sx+0.5f, .3f, sy+0.5f); 
		moonbaseAssaultServer.actuallyAddEntity(medi);

		FloorOrCeiling ceiling = new FloorOrCeiling(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), "Ceiling",      sx,      MoonbaseAssaultServer.CEILING_HEIGHT+0.5f, sy,   w, .5f, d, MATextures.CORRIDOR, false);
		moonbaseAssaultServer.actuallyAddEntity(ceiling);
		this.totalCeilings++;
		/*
		if (w > 4 || d > 4) {
			// Ceiling greeble
			FloorOrCeiling ceiling2 = new FloorOrCeiling(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), "Ceiling", sx+.95f, MoonbaseAssaultServer.CEILING_HEIGHT+0.5f, sy+1, 1, .8f, 1, MATextures.CEILING_GREEBLE, false);
			moonbaseAssaultServer.actuallyAddEntity(ceiling2);
		}
		 */

		// Mark area as handled
		for (int y=sy ; y<ey ; y++) {
			for (int x=sx ; x<ex ; x++) {
				mapCode[x][y] = HANDLED;
				floorSquares.add(new Point(x, y));
			}
		}

	}


	private Point acrossThenDown(int sx, int sy) {
		int ex;
		for (ex=sx ; ex<mapsize ; ex++) {
			if (mapCode[ex][sy] != INT_FLOOR) {
				break;
			}
		}
		// Cover rect
		boolean breakout = false;
		int ey;
		for (ey=sy+1 ; ey<mapsize ; ey++) {
			for (int x=sx ; x<ex ; x++) {
				if (mapCode[x][ey] != INT_FLOOR) {
					breakout = true;
					break;
				}
			}
			if (breakout) {
				break;
			}
		}

		return new Point(ex, ey);

	}


	private Point downThenAcross(int sx, int sy) {
		int ey;
		for (ey=sy ; ey<mapsize ; ey++) {
			if (mapCode[sx][ey] != INT_FLOOR) {
				break;
			}
		}
		// Cover rect
		boolean breakout = false;
		int ex;
		for (ex=sx+1 ; ex<mapsize ; ex++) {
			for (int y=sy ; y<ey ; y++) {
				if (mapCode[ex][y] != INT_FLOOR) {
					breakout = true;
					break;
				}
			}
			if (breakout) {
				break;
			}
		}

		return new Point(ex, ey);

	}


	private void printMap() {
		// print map
		for (int y=0 ; y<mapsize ; y++) {
			for (int x=0 ; x<mapsize ; x++) {
				if (mapCode[x][y] == WALL) {
					System.out.print("X");
				} else if (mapCode[x][y] == INT_FLOOR) {
					System.out.print(".");
				} else if (mapCode[x][y] == EXT_FLOOR) {
					System.out.print(",");
				} else if (mapCode[x][y] == COMPUTER) {
					System.out.print("C");
				} else if (mapCode[x][y] == DOOR_LR) {
					System.out.print("L");
				} else if (mapCode[x][y] == DOOR_UD) {
					System.out.print("U");
				} else if (mapCode[x][y] == HANDLED) {
					System.out.print("H");
				} else {
					System.out.print(" ");
				}					
			}
			System.out.println("");
		}

		Globals.p("Created " + this.totalWalls + " walls, " + this.totalFloors + " floors, " + this.totalCeilings + " ceilings, " + totalDoors + " doors.");

	}
}

