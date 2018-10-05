package com.scs.moonbaseassault.entities;

import com.jme3.math.Vector3f;
import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.moonbaseassault.models.SoldierModel;
import com.scs.moonbaseassault.server.ai.ShootingSoldierAI3;
import com.scs.stevetech1.entities.AbstractBullet;
import com.scs.stevetech1.shared.IEntityController;

public class MA_AISuperSoldier extends AbstractAISoldier { // Not currently used.
	
	public MA_AISuperSoldier(IEntityController _game, int id, float x, float y, float z, byte _side, boolean friend, String name, int startAnimCode) {
		super(_game, id, MoonbaseAssaultClientEntityCreator.AI_SUPER_SOLDIER, x, y, z, _side, 
				new SoldierModel(_game.getAssetManager(), false, _side, friend), name, startAnimCode);

		if (_game.isServer()) {
			boolean attacker = side == 1;
			ai = new ShootingSoldierAI3(this, attacker, !attacker);
		}
	}

	
	@Override
	protected AbstractBullet createAIBullet(Vector3f pos, Vector3f dir) {
		LaserBullet bullet = new LaserBullet(game, game.getNextEntityID(), -1, this, pos, dir,  side, null);
		return bullet;
	}

	
	@Override
	public String getDebrisTexture() {
		return "Textures/blood.png";
	}

	
	@Override
	public float getMinDebrisSize() {
		return 0.001f;
	}


	@Override
	public float getMaxDebrisSize() {
		return 0.004f;
	}

	
}
