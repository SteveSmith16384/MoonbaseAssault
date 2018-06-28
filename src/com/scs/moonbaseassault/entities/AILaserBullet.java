package com.scs.moonbaseassault.entities;

import java.util.HashMap;

import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;
import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.moonbaseassault.server.MoonbaseAssaultServer;
import com.scs.stevetech1.components.IDebrisTexture;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.entities.AbstractAIBullet;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.models.BeamLaserModel;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

public class AILaserBullet extends AbstractAIBullet {

	private static final float LENGTH = .7f;
	public static final float RANGE = 30f;
	public static final float SPEED = 15f;
	private static final boolean USE_CYLINDER = true;

	public AILaserBullet(IEntityController _game, int id, int side, float x, float y, float z, IEntity _shooter, Vector3f dir) {
		super(_game, id, MoonbaseAssaultClientEntityCreator.AI_LASER_BULLET, x, y, z, "LaserBullet", side, _shooter, dir, true, SPEED, RANGE);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("side", side);
			creationData.put("shooterID", shooter.getID());
			creationData.put("dir", dir);
		}

		this.getMainNode().setUserData(Globals.ENTITY, this);

	}


	@Override
	protected void createBulletModel(Vector3f dir) {
		Spatial laserNode = null;
		if (USE_CYLINDER) {
			Vector3f origin = Vector3f.ZERO;
			laserNode = BeamLaserModel.Factory(game.getAssetManager(), origin, origin.add(dir.mult(LENGTH)), ColorRGBA.Pink, !game.isServer(), "Textures/yellowsun.jpg", MoonbaseAssaultServer.LASER_DIAM, Globals.BULLETS_CONES);
		} else {
			Mesh sphere = null;
			sphere = new Sphere(8, 8, 0.02f, true, false);
			laserNode = new Geometry("DebuggingSphere", sphere);

			TextureKey key3 = null;
			key3 = new TextureKey( "Textures/sun.jpg");
			Texture tex3 = game.getAssetManager().loadTexture(key3);
			Material floor_mat = null;
			floor_mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");
			floor_mat.setTexture("DiffuseMap", tex3);
			laserNode.setMaterial(floor_mat);
		}

		//laserNode.setShadowMode(ShadowMode.Cast);
		this.mainNode.attachChild(laserNode);
		mainNode.setUserData(Globals.ENTITY, this);

	}


	@Override
	public float getDamageCaused() {
		float dist = Math.max(1, this.getDistanceTravelled());
		return ((RANGE-dist) / dist) * 1;
	}


	@Override
	public void collided(PhysicalEntity pe) {
		if (game.isServer()) {
			AbstractGameServer server = (AbstractGameServer)game;
			String tex = "Textures/sun.jpg";
			float minSize = .01f;
			float maxSize = .04f;
			if (pe instanceof IDebrisTexture) {
				IDebrisTexture dt = (IDebrisTexture)pe;
				tex = dt.getDebrisTexture();
				minSize = dt.getMinDebrisSize();
				maxSize = dt.getMaxDebrisSize();
			}
			server.sendExplosion(this.getWorldTranslation(), 4, .8f, 1.2f, minSize, maxSize, tex);
			game.playSound("todo", getWorldTranslation(), Globals.DEF_VOL, false);
		}
		this.remove();
	}

}
