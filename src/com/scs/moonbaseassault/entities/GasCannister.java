package com.scs.moonbaseassault.entities;

import java.util.HashMap;

import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Cylinder;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.components.IDamagable;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.jme.JMEAngleFunctions;
import com.scs.stevetech1.jme.JMEModelFunctions;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

public class GasCannister extends PhysicalEntity implements IDamagable {

	private static final float HEIGHT = 0.4f;
	private static final float RAD = 0.1f;
	private float health = 1;

	public GasCannister(IEntityController _game, int id, float x, float y, float z) {
		super(_game, id, MoonbaseAssaultClientEntityCreator.GAS_CANNISTER, "GasCannister", false, true, true);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
		}

		Cylinder cyl = new Cylinder(2, 8, RAD, HEIGHT, true);
		Geometry geometry = new Geometry("GasCannister", cyl);
		if (!_game.isServer()) {
			geometry.setShadowMode(ShadowMode.CastAndReceive);

			TextureKey key3 = new TextureKey("Textures/gascannister.jpg");
			key3.setGenerateMips(true);
			Texture tex3 = game.getAssetManager().loadTexture(key3);
			tex3.setWrap(WrapMode.Repeat);
			Material floorMat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");  // create a simple material
			floorMat.setTexture("DiffuseMap", tex3);
			geometry.setMaterial(floorMat);
		}
		JMEAngleFunctions.rotateToWorldDirection(geometry, new Vector3f(0, 1, 0));
		geometry.setLocalTranslation(RAD, -HEIGHT, RAD);
		JMEModelFunctions.moveYOriginTo(geometry, 0f);

		this.mainNode.attachChild(geometry);
		mainNode.setLocalTranslation(x, y, z);
		mainNode.updateModelBound();

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this, game.getPhysicsController(), false, this);
		simpleRigidBody.setNeverMoves(true);

		geometry.setUserData(Globals.ENTITY, this);
	}


	@Override
	public void damaged(float amt, IEntity collider, String reason) {
		//Globals.p("Gas can hit!");
		if (!this.isMarkedForRemoval()) { // Prevent re-exploding
			this.health -= amt;
			if (this.health <= 0) {
				AbstractGameServer server  = (AbstractGameServer)game;
				String tex = "Textures/sun.jpg";
				server.sendExplosionShards(this.getWorldTranslation(), 30, 2.8f, 5.2f, .01f, .04f, tex);
				server.sendExpandingSphere(this.getWorldTranslation());
				server.damageSurroundingEntities(this, 4f, 50);
				game.markForRemoval(this);
			}
		}
	}


	@Override
	public byte getSide() {
		return -1;
	}


	@Override
	public float getHealth() {
		return health;
	}


	@Override
	public void updateClientSideHealth(int amt) {
		// Do nothing
		
	}


	@Override
	public boolean canBeDamaged() {
		return true;
	}
	

}
