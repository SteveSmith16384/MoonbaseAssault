package com.scs.moonbaseassault.weapons;

import com.jme3.math.Vector3f;
import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.moonbaseassault.entities.PlayersGrenade;
import com.scs.stevetech1.components.ICanShoot;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.shared.IAbility;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.stevetech1.weapons.AbstractMagazineGun;

public class GrenadeLauncher extends AbstractMagazineGun implements IAbility {

	private static final int MAG_SIZE = 6;

	//private LinkedList<PlayersGrenade> ammoCache = new LinkedList<PlayersGrenade>();

	public GrenadeLauncher(IEntityController game, int id, int playerID, ICanShoot owner, int avatarID, int num, ClientData _client) {
		super(game, id, MoonbaseAssaultClientEntityCreator.GRENADE_LAUNCHER, playerID, owner, avatarID, num, "GrenadeLauncher", 1, 3, MAG_SIZE, _client);
		
	}

/*
	@Override
	public HashMap<String, Object> getCreationData() {
		return super.creationData;
	}
*/
/*
	@Override
	public void remove() {
		// Remove all owned bullets
		while (!ammoCache.isEmpty()) {
			PlayersGrenade g = ammoCache.remove();
			g.remove();
		}
		super.remove();
	}
*/

	@Override
	protected PlayersGrenade createBullet(int entityid, int playerID, IEntity _shooter, Vector3f startPos, Vector3f _dir, int side) {
		return new PlayersGrenade(game, entityid, playerID, _shooter, startPos, _dir, side, client);
	}

/*
	@Override
	public void addToCache(PlayersGrenade o) {
		this.ammoCache.add(o);
	}


	@Override
	public void removeFromCache(PlayersGrenade o) {
		this.ammoCache.remove(o);
	}

/*
	@Override
	protected void emptyMagazine() {
		while (!ammoCache.isEmpty()) {
			PlayersGrenade g = ammoCache.remove();
			g.remove();
		}
		
	}
*/
}

