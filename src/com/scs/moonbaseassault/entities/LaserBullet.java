package com.scs.moonbaseassault.entities;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.scs.moonbaseassault.MASounds;
import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.moonbaseassault.server.MoonbaseAssaultServer;
import com.scs.stevetech1.components.IDebrisTexture;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.INotifiedOfCollision;
import com.scs.stevetech1.entities.AbstractBullet;
import com.scs.stevetech1.entities.DebuggingSphere;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.models.BeamLaserModel;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

public class LaserBullet extends AbstractBullet implements INotifiedOfCollision {

	public static final float SPEED = 15f;
	private static final float LENGTH = .7f;
	public static final float RANGE = 30f;

	public LaserBullet(IEntityController _game, int id, int playerOwnerId, IEntity _shooter, Vector3f startPos, Vector3f _dir, byte _side, ClientData _client) {
		super(_game, id, MoonbaseAssaultClientEntityCreator.LASER_BULLET, "LaserBullet", playerOwnerId, _shooter, startPos, _dir, _side, _client, true, SPEED, RANGE);

	}


	@Override
	protected void createModelAndSimpleRigidBody(Vector3f dir) {
		Vector3f origin = Vector3f.ZERO;
		Spatial laserNode = BeamLaserModel.Factory(game.getAssetManager(), origin, origin.add(dir.mult(LENGTH)), ColorRGBA.Pink, !game.isServer(), "Textures/cells3.png", MoonbaseAssaultServer.LASER_DIAM, Globals.BULLETS_CONES);
		//laserNode.setShadowMode(ShadowMode.Cast);
		this.mainNode.attachChild(laserNode);
		
		if (!game.isServer()) {
			game.playSound(MASounds.SFX_LASER_BULLET_FIRED, this.getID(), this.origin, Globals.DEF_VOL, false);
		}

		// Note that we don't create a SRB since we use Rays

	}
	
	
	@Override
	public void remove() {
		super.remove();
	}

	
	@Override
	public void notifiedOfCollision(PhysicalEntity pe) {
		if (game.isServer()) {
			String tex = "Textures/sun.jpg";
			if (pe instanceof IDebrisTexture) {
				IDebrisTexture dt = (IDebrisTexture)pe;
				tex = dt.getDebrisTexture();
			}
			AbstractGameServer server = (AbstractGameServer)game;
			server.sendExplosionShards(this.getWorldTranslation(), 4, .8f, 1.2f, .005f, .02f, tex);
			
			if (Globals.SHOW_BULLET_COLLISION_POS) {
				// Create debugging sphere
				Vector3f pos = this.getWorldTranslation();
				DebuggingSphere ds = new DebuggingSphere(game, game.getNextEntityID(), pos.x, pos.y, pos.z, true, false);
				game.addEntity(ds);
			}
		} else {
			game.playSound(MASounds.SFX_EXPLOSION, -1, getWorldTranslation(), Globals.DEF_VOL, false);
		}
		game.markForRemoval(this);
	}


	@Override
	public float getDamageCaused() {
		//return ((RANGE-this.getDistanceTravelled()) / this.getDistanceTravelled()) * 10;
		float dam = (((RANGE-this.getDistanceTravelled()) / this.getDistanceTravelled()) * 5)+5;
		Globals.p(this + " damage: " + dam);
		return dam;
	}


}
