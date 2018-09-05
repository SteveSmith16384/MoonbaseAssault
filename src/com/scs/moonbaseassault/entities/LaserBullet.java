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

	public LaserBullet(IEntityController _game, int id, int playerOwnerId, IEntity _shooter, Vector3f startPos, Vector3f _dir, int _side, ClientData _client) {
		super(_game, id, MoonbaseAssaultClientEntityCreator.LASER_BULLET, "LaserBullet", playerOwnerId, _shooter, startPos, _dir, _side, _client, true, SPEED, RANGE);

		//this.getMainNode().setUserData(Globals.ENTITY, this);

	}


	@Override
	protected void createModelAndSimpleRigidBody(Vector3f dir) {
		Spatial laserNode = null;
		Vector3f origin = Vector3f.ZERO;
		laserNode = BeamLaserModel.Factory(game.getAssetManager(), origin, origin.add(dir.mult(LENGTH)), ColorRGBA.Pink, !game.isServer(), "Textures/cells3.png", MoonbaseAssaultServer.LASER_DIAM, Globals.BULLETS_CONES);

		//laserNode.setShadowMode(ShadowMode.Cast);
		this.mainNode.attachChild(laserNode);

	}
	

	@Override
	public void collided(PhysicalEntity pe) {
		if (game.isServer()) {
			//Globals.p("PlayerLaserBullet collided");
			AbstractGameServer server = (AbstractGameServer)game;
			String tex = "Textures/sun.jpg";
			if (pe instanceof IDebrisTexture) {
				IDebrisTexture dt = (IDebrisTexture)pe;
				tex = dt.getDebrisTexture();
			}
			server.sendExplosion(this.getWorldTranslation(), 4, .8f, 1.2f, .005f, .02f, tex);
			game.playSound(MASounds.SFX_EXPLOSION, -1, getWorldTranslation(), Globals.DEF_VOL, false);
			
			if (Globals.SHOW_BULLET_COLLISION_POS) {
				// Create debugging sphere
				Vector3f pos = this.getWorldTranslation();
				DebuggingSphere ds = new DebuggingSphere(game, game.getNextEntityID(), pos.x, pos.y, pos.z, true, false);
				game.addEntity(ds);
			}

		}
		//this.remove();
		game.markForRemoval(this.getID());
	}

}
