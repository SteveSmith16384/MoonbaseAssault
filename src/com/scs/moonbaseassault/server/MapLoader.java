package com.scs.moonbaseassault.server;

import java.awt.Point;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import com.jme3.math.Vector3f;
import com.scs.moonbaseassault.entities.Computer;
import com.scs.moonbaseassault.entities.DestroyedComputer;
import com.scs.moonbaseassault.entities.Floor;
import com.scs.moonbaseassault.entities.GasCannister;
import com.scs.moonbaseassault.entities.GenericFloorTex;
import com.scs.moonbaseassault.entities.MapBorder;
import com.scs.moonbaseassault.entities.MoonbaseWall;
import com.scs.moonbaseassault.entities.SlidingDoor;
import com.scs.moonbaseassault.entities.SpaceCrate;
import com.scs.stevetech1.jme.JMEAngleFunctions;
import com.scs.stevetech1.server.Globals;

import ssmith.lang.NumberFunctions;

public class MapLoader {

	private static final int HANDLED = 0;
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
	private int totalWalls, totalFloors, totalCeilings, numCrates;
	private MoonbaseAssaultServer moonbaseAssaultServer;
	public int scannerData[][];
	public ArrayList<Point>[] deploySquares;

	public Point firstInteriorFloor = null;
	public ArrayList<Point> floorSquares;// = new ArrayList<Point>()[2];

	public MapLoader(MoonbaseAssaultServer _moonbaseAssaultServer) {
		super();

		moonbaseAssaultServer = _moonbaseAssaultServer;
		deploySquares = new ArrayList[2];
		this.deploySquares[0] = new ArrayList<Point>();
		this.deploySquares[1] = new ArrayList<Point>();
	}


	public void loadMap(String s) throws FileNotFoundException, IOException, URISyntaxException {
		String text = new String(Files.readAllBytes(Paths.get(getClass().getResource(s).toURI())));
		String[] lines = text.split(System.lineSeparator());

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
							if (this.firstInteriorFloor == null && x < 30) {
								this.firstInteriorFloor = new Point(x, lineNum); // Put soldier behind door
							}
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

		{
			// Vertical walls
			int y = 0;
			while (y < mapsize-1) {
				int x = 0;
				while (x < mapsize) {
					if (mapCode[x][y] == WALL) {// && handled[x][y+1] == WALL) {
						checkForVerticalWalls(x, y);
					}				
					x++;
				}						
				y++;
			}
		}

		// Doors && comps
		{
			for (int y=0 ; y<mapsize ; y++) {
				for (int x=0 ; x<mapsize ; x++) {
					if (mapCode[x][y] == DOOR_LR) {
						SlidingDoor door = new SlidingDoor(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), x, 0, y, 1, MoonbaseAssaultServer.CEILING_HEIGHT, "Textures/door_lr.png", 0);
						moonbaseAssaultServer.actuallyAddEntity(door);
						mapCode[x][y] = INT_FLOOR; // So we create a floor below it
						
						GenericFloorTex gft = new GenericFloorTex(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), x-.5f, INT_FLOOR_HEIGHT + 0.01f, y+.5f, 1f, 1f, "Textures/floor4.jpg");
						moonbaseAssaultServer.actuallyAddEntity(gft);
					} else if (mapCode[x][y] == DOOR_UD) {
						SlidingDoor door = new SlidingDoor(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), x, 0, y, 1, MoonbaseAssaultServer.CEILING_HEIGHT, "Textures/door_lr.png", 270);
						moonbaseAssaultServer.actuallyAddEntity(door);
						mapCode[x][y] = INT_FLOOR; // So we create a floor below it
						
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
		doInteriorFloorsAndCeilings();

		// One big moon floor
		Floor moonrock = new Floor(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), "Big Ext Floor", 0, 0, 0, mapsize, .5f, mapsize, "Textures/moonrock.png");
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

		//Globals.p("Finished.  Created " + this.totalWalls + " walls, " + this.totalFloors + " floors, " + this.totalCeilings + " ceilings, " + numCrates + " spacecrates.");
		
		Vector3f down = new Vector3f(0, -1, 0);
		// Scenery
		for (int i=0 ; i<30 ; i++) {
			Point p = this.floorSquares.remove(NumberFunctions.rnd(0,  floorSquares.size()-1));
			GasCannister gas = new GasCannister(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), p.x+0.5f, INT_FLOOR_HEIGHT + 0.1f, p.y+0.5f);
			moonbaseAssaultServer.actuallyAddEntity(gas);
			Vector3f dir = JMEAngleFunctions.getRandomDirection_4();
			moonbaseAssaultServer.moveEntityUntilItHitsSomething(gas, dir);
			Vector3f dir2 = JMEAngleFunctions.turnRight(dir);
			moonbaseAssaultServer.moveEntityUntilItHitsSomething(gas, dir2);
			moonbaseAssaultServer.moveEntityUntilItHitsSomething(gas, down, 0.1f);
			Globals.p("Gas can at " + gas.getWorldTranslation());
		}

		for (int i=0 ; i<30 ; i++) {
			Point p = this.floorSquares.remove(NumberFunctions.rnd(0,  floorSquares.size()-1));
			float size = .2f; //Must be thin enough for them to be able to move past each other when positioning //  NumberFunctions.rndFloat(.2f, .3f);
			int rot = NumberFunctions.rnd(0,  90);
			SpaceCrate spacecrate = new SpaceCrate(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), p.x+.5f, size+0.1f, p.y+.5f, size, size, size, "Textures/spacecrate1.png", rot);
			moonbaseAssaultServer.actuallyAddEntity(spacecrate);
			Vector3f dir = JMEAngleFunctions.getRandomDirection_4();
			moonbaseAssaultServer.moveEntityUntilItHitsSomething(spacecrate, dir);
			Vector3f dir2 = JMEAngleFunctions.turnRight(dir);
			moonbaseAssaultServer.moveEntityUntilItHitsSomething(spacecrate, dir2);
			moonbaseAssaultServer.moveEntityUntilItHitsSomething(spacecrate, down, 0.1f);
			Globals.p("SpaceCrate at " + spacecrate.getWorldTranslation());
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
		MoonbaseWall wall = new MoonbaseWall(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), sx, 0f, sy, x-sx+1, MoonbaseAssaultServer.CEILING_HEIGHT, 1, "Textures/ufo2_03.png");
		moonbaseAssaultServer.actuallyAddEntity(wall);
		totalWalls++;
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
		MoonbaseWall wall = new MoonbaseWall(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), sx, 0f, sy, 1, MoonbaseAssaultServer.CEILING_HEIGHT, y-sy+1, "Textures/ufo2_03.png");
		moonbaseAssaultServer.actuallyAddEntity(wall);
		totalWalls++;
	}


	private void doInteriorFloorsAndCeilings() {
		floorSquares = new ArrayList<Point>();
		boolean found = true;
		while (found) {
			found = false;
			for (int y=0 ; y<mapsize ; y++) {
				for (int x=0 ; x<mapsize ; x++) {
					if (mapCode[x][y] == INT_FLOOR) {
						found = true;
						interiorFloorAndCeiling(x, y);
					}
				}
			}
		}
	}


	private void interiorFloorAndCeiling(int sx, int sy) {
		// Go across
		int ex;
		for (ex=sx ; ex<mapsize ; ex++) {
			if (mapCode[ex][sy] != INT_FLOOR) {
				break;
			}
			mapCode[ex][sy] = HANDLED;
		}
		// Cover rect
		boolean breakout = false;
		int ey;
		for (ey=sy+1 ; ey<mapsize ; ey++) {
			for (int x=sx ; x<=ex ; x++) {
				if (mapCode[x][sy] != INT_FLOOR) {
					breakout = true;
					break;
				}
			}
			if (breakout) {
				break;
			}
		}
		int w = ex-sx;
		int d = ey-sy;
		
		String floorTex = "escape_hatch.jpg";
		/*switch (NumberFunctions.rnd(1, 8)) {
		case 1: floorTex = "escape_hatch.jpg"; break;
		case 2: floorTex = "corridor.jpg"; break;
		case 3: floorTex = "floor2.jpg"; break;
		case 4: floorTex = "floor3.jpg"; break;
		case 5: floorTex = "floor5.jpg"; break;
		case 6: floorTex = "floor006.png"; break;
		case 7: floorTex = "floor015.png"; break;
		case 8: floorTex = "metalfloor1.jpg"; break;
		default: throw new RuntimeException("Invalid floor tex");
		}*/
		
		Floor floor = new Floor(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), "Int floor", sx, INT_FLOOR_HEIGHT, sy, w, .5f, d, "Textures/" + floorTex);//"Textures/escape_hatch.jpg");
		moonbaseAssaultServer.actuallyAddEntity(floor);
		this.totalFloors++;

		// Space crate
		/*float size = .25f;
		SpaceCrate crate = new SpaceCrate(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), sx+.5f, size, sy+.5f, size, size, size, "Textures/spacecrate1.png", 0);
		moonbaseAssaultServer.actuallyAddEntity(crate);
		if (moveEntityUntilItHitsSomething(crate, new Vector3f(1, 0, 0))) {
			numCrates++;
		}*/


		Floor ceiling = new Floor(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), "Ceiling", sx, MoonbaseAssaultServer.CEILING_HEIGHT+0.5f, sy, w, .5f, d, "Textures/ufo2_03.png");
		moonbaseAssaultServer.actuallyAddEntity(ceiling);
		this.totalCeilings++;

		// Mark area as handled
		for (int y=sy ; y<ey ; y++) {
			for (int x=sx ; x<ex ; x++) {
				mapCode[x][y] = HANDLED;
				floorSquares.add(new Point(x, y));
			}
		}

	}


	private void doExteriorFloors() {
		boolean found = true;
		while (found) {
			found = false;
			for (int y=0 ; y<mapsize ; y++) {
				for (int x=0 ; x<mapsize ; x++) {
					if (mapCode[x][y] == EXT_FLOOR) {
						found = true;
						exteriorFloor(x, y);
					}
				}
			}
		}

	}


	private void exteriorFloor(int sx, int sy) {
		// Go across
		int ex;
		for (ex=sx ; ex<mapsize ; ex++) {
			if (mapCode[ex][sy] != EXT_FLOOR) {
				break;
			}
			mapCode[ex][sy] = HANDLED;
		}
		// Go cover rect
		boolean breakout = false;
		int ey;
		for (ey=sy+1 ; ey<mapsize ; ey++) {
			for (int x=sx ; x<=ex ; x++) {
				if (mapCode[x][sy] != EXT_FLOOR) {
					breakout = true;
					break;
				}
			}
			if (breakout) {
				break;
			}
		}
		Floor moonrock = new Floor(moonbaseAssaultServer, moonbaseAssaultServer.getNextEntityID(), "Ext Floor", sx, 0f, sy, ex-sx, .5f, ey-sy, "Textures/moonrock.png");
		moonbaseAssaultServer.actuallyAddEntity(moonrock);
		this.totalFloors++;

		// Mark area as handled
		for (int y=sy ; y<ey ; y++) {
			for (int x=sx ; x<ex ; x++) {
				mapCode[x][sy] = HANDLED;
			}
		}

	}


}

