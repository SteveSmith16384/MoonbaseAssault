package com.scs.moonbaseassault.server.ai;

import java.awt.Point;
import java.util.List;

import com.jme3.math.Vector3f;
import com.scs.moonbaseassault.server.MoonbaseAssaultServer;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.Globals;

import ssmith.astar.AStar;
import ssmith.astar.WayPoints;
import ssmith.lang.NumberFunctions;

public class FindComputerThread extends Thread {

	private MoonbaseAssaultServer game;
	private PhysicalEntity unit;
	public WayPoints route;
	
	public static int totalThreads = 0;

	public FindComputerThread(MoonbaseAssaultServer _game, PhysicalEntity _unit) {
		super("FindComputerThread")	;

		this.setDaemon(true);

		game = _game;
		unit = _unit;
	}


	public void run() {
		synchronized (game) { // to ensure they are checked one by one
			totalThreads++;
			if (Globals.DEBUG_SLOW_MA) {
				Globals.p("FindComputerThreads=" + totalThreads);
			}
			int closestDist = 9999;
			AStar astar = new AStar(game);
			List<Point> comps = game.getComputerSquares();
			for (int i=0 ; i<comps.size() ; i++) { // Only try so many times
				Point p = comps.get(NumberFunctions.rnd(0, comps.size()-1)); // Chose random square
				Vector3f pos = unit.getWorldTranslation();
				astar.findPath((int)pos.x, (int)pos.z, p.x, p.y, closestDist, false);
				if (astar.wasSuccessful()) {
					if (astar.getRoute().size() < closestDist) {
						closestDist = astar.getRoute().size();
						route = astar.getRoute();
						break;
					}
				}
			}
			totalThreads--;
			if (Globals.DEBUG_SLOW_MA) {
				Globals.p("FindComputerThreads=" + totalThreads);
			}
		}
	}

}