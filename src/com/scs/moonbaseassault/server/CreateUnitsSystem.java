package com.scs.moonbaseassault.server;

import com.scs.moonbaseassault.MoonbaseAssaultGlobals;
import com.scs.moonbaseassault.entities.AbstractAISoldier;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.entities.AbstractServerAvatar;

import ssmith.lang.NumberFunctions;
import ssmith.util.RealtimeInterval;

/**
 * Notes:
 * - Players are worth more than AI
 * - Need more defenders since they have to cover a bigger area
 * - Minimum of 8 defenders, minimum of 4 attackers
 * - Minimum of double the defenders than attackers
 */
public class CreateUnitsSystem {

	private RealtimeInterval recalcNumUnitsInterval = new RealtimeInterval(1000 * 10);
	private MoonbaseAssaultServer server;
	private String[] names;

	public CreateUnitsSystem(MoonbaseAssaultServer _server, String[] _names) {
		server = _server;
		names = _names;
	}


	private String getName() {
		return names[NumberFunctions.rnd(0, names.length-1)] + " " + names[NumberFunctions.rnd(0, names.length-1)];
	}
	
	
	public void process() {
		if (recalcNumUnitsInterval.hitInterval()) {

			int numAttackers = 0;
			int numDefenders = 0;

			for (int i=0 ; i<server.entitiesForProcessing.size() ; i++) {
				IEntity e = server.entitiesForProcessing.get(i);
				if (e instanceof AbstractServerAvatar) {
					AbstractServerAvatar asa = (AbstractServerAvatar)e;
					if (asa.side == MoonbaseAssaultGlobals.SIDE_ATTACKERS) { // Attacker
						numAttackers += 2;
					} else if (asa.side == MoonbaseAssaultGlobals.SIDE_DEFENDERS) { // Defender
						numDefenders += 2;
					} else {
						throw new RuntimeException("Invalid side: " + asa.side);
					}
				} else if (e instanceof AbstractAISoldier) {
					AbstractAISoldier ai = (AbstractAISoldier)e;
					if (ai.side == MoonbaseAssaultGlobals.SIDE_ATTACKERS) { // Attacker
						numAttackers++;
					} else if (ai.side == MoonbaseAssaultGlobals.SIDE_DEFENDERS) { // Defender
						numDefenders++;
					} else {
						throw new RuntimeException("Invalid side: " + ai.side);
					}
				}
			}

			// Create attackers
			while (numAttackers < 5) { // 2 players, one AI?
				server.addAISoldier(MoonbaseAssaultGlobals.SIDE_ATTACKERS, getName());
				numAttackers++;
			}

			// Create defenders
			while (numDefenders < numAttackers*2 || numDefenders < 8) {
				server.addAISoldier(MoonbaseAssaultGlobals.SIDE_DEFENDERS, getName());
				numDefenders++;
			}

		}
	}

}
