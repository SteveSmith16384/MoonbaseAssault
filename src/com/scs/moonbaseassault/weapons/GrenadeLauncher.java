package com.scs.moonbaseassault.weapons;

import com.jme3.math.Vector3f;
import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.moonbaseassault.entities.Grenade;
import com.scs.stevetech1.components.ICanShoot;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.shared.IAbility;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.stevetech1.weapons.AbstractMagazineGun;

public class GrenadeLauncher extends AbstractMagazineGun implements IAbility {

	private static final int MAG_SIZE = 6;

	public GrenadeLauncher(IEntityController game, int id, int playerID, ICanShoot owner, int avatarID, byte num, ClientData _client) {
		super(game, id, MoonbaseAssaultClientEntityCreator.GRENADE_LAUNCHER, playerID, owner, avatarID, num, "GrenadeLauncher", 1, 3, MAG_SIZE, _client);
		
	}


	@Override
	protected Grenade createBullet(int entityid, int playerID, IEntity _shooter, Vector3f startPos, Vector3f _dir, byte side) {
		return new Grenade(game, entityid, playerID, _shooter, startPos, _dir, side, client);
	}

}

