package com.scs.moonbaseassault.shared;

import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.AbstractCollisionValidator;

public class MoonbaseAssaultCollisionValidator extends AbstractCollisionValidator {

	@Override
	public boolean canCollide(PhysicalEntity pa, PhysicalEntity pb) {
		if (super.canCollide(pa, pb) == false) {
			return false;
		}
		
		// Explosion shards don't collide with each other
		if (pa.type == Globals.EXPLOSION_SHARD && pb.type == Globals.EXPLOSION_SHARD) {
			return false;
		}

		// Explosion shards don't collide with player
		if ((pa.type == Globals.EXPLOSION_SHARD && pb.type == MoonbaseAssaultClientEntityCreator.SOLDIER_AVATAR) || (pb.type == Globals.EXPLOSION_SHARD && pa.type == MoonbaseAssaultClientEntityCreator.SOLDIER_AVATAR)) {
			return false;
		}

		// Explosion shards don't collide with AI
		if ((pa.type == Globals.EXPLOSION_SHARD && pb.type == MoonbaseAssaultClientEntityCreator.AI_SOLDIER) || (pb.type == Globals.EXPLOSION_SHARD && pa.type == MoonbaseAssaultClientEntityCreator.AI_SOLDIER)) {
			return false;
		}

		// Sliding doors shouldn't collide with floor/ceiling
		if ((pa.type == MoonbaseAssaultClientEntityCreator.FLOOR_OR_CEILING && pb.type == MoonbaseAssaultClientEntityCreator.DOOR) || pa.type == MoonbaseAssaultClientEntityCreator.DOOR && pb.type == MoonbaseAssaultClientEntityCreator.FLOOR_OR_CEILING) {
			return false;
		}
		// Sliding doors shouldn't collide with wall
		if ((pa.type == MoonbaseAssaultClientEntityCreator.WALL && pb.type == MoonbaseAssaultClientEntityCreator.DOOR) || pa.type == MoonbaseAssaultClientEntityCreator.DOOR && pb.type == MoonbaseAssaultClientEntityCreator.WALL) {
			return false;
		}		

		return true;
	}

}
