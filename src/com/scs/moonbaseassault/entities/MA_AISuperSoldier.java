package com.scs.moonbaseassault.entities;

import com.jme3.math.Vector3f;
import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.moonbaseassault.models.SoldierModel;
import com.scs.moonbaseassault.server.ai.ShootingSoldierAI3;
import com.scs.stevetech1.entities.AbstractAIBullet;
import com.scs.stevetech1.shared.IEntityController;

public class MA_AISuperSoldier extends AbstractAISoldier {
	
	public MA_AISuperSoldier(IEntityController _game, int id, float x, float y, float z, int _side, boolean friend, int csInitialAnimCode, String name) {
		super(_game, id, MoonbaseAssaultClientEntityCreator.AI_SUPER_SOLDIER, x, y, z, _side, 
				new SoldierModel(_game.getAssetManager(), _side, false, friend), csInitialAnimCode, name);

		if (_game.isServer()) {
			boolean attacker = side == 1;
			ai = new ShootingSoldierAI3(this, attacker, !attacker);
		}
	}

	
	@Override
	protected AbstractAIBullet createBullet(Vector3f pos, Vector3f dir) {
		AILaserBullet bullet = new AILaserBullet(game, game.getNextEntityID(), side, pos.x, pos.y, pos.z, this, dir);
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

	
	@Override
	public void updateClientSideHealth(int amt) {
		
	}
	

}
