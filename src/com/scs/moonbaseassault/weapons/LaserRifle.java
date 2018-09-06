package com.scs.moonbaseassault.weapons;

import com.jme3.math.Vector3f;
import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.moonbaseassault.entities.LaserBullet;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.shared.IAbility;
import com.scs.stevetech1.shared.IEntityController;
import com.scs.stevetech1.weapons.AbstractMagazineGun;

/*
 * This gun shoots physical laser bolts
 */
public class LaserRifle extends AbstractMagazineGun implements IAbility {

	public LaserRifle(IEntityController game, int id, int playerID, AbstractAvatar owner, int avatarID, byte abilityNum, ClientData client) {
		super(game, id, MoonbaseAssaultClientEntityCreator.LASER_RIFLE, playerID, owner, avatarID, abilityNum, "Laser Rifle", .2f, 2, 10, client);

	}
/*

	@Override
	public HashMap<String, Object> getCreationData() {
		return super.creationData;
	}

/*
	@Override
	public void addToCache(PlayersLaserBullet o) {
		this.ammoCache.add(o);
	}


	@Override
	public void removeFromCache(PlayersLaserBullet o) {
		this.ammoCache.remove(o);
	}

/*
	public void remove() {
		while (!ammoCache.isEmpty()) {
			PlayersLaserBullet g = ammoCache.remove();
			g.remove();
		}
		super.remove();
	}
*/

	@Override
	protected LaserBullet createBullet(int entityid, int playerID, IEntity _shooter, Vector3f startPos, Vector3f _dir, byte side) {
		return new LaserBullet(game, entityid, playerID, _shooter, startPos, _dir, side, client);

	}
	
/*
	@Override
	public int getBulletsInMag() {
		return this.ammoCache.size();
	}

/*
	@Override
	protected void emptyMagazine() {
		while (!ammoCache.isEmpty()) {
			PlayersLaserBullet g = ammoCache.remove();
			g.remove();
		}
		
	}
*/

}
