package com.scs.moonbaseassault.entities;

import com.jme3.asset.TextureKey;
import com.jme3.bounding.BoundingBox;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import com.jme3.texture.Texture;
import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.client.IClientApp;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.entities.AbstractBullet;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.ClientData;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

public class Grenade extends AbstractBullet {

	private static final float DURATION = 3f;

	private float timeLeft = DURATION;

	public Grenade(IEntityController _game, int id, int playerOwnerId, IEntity _shooter, Vector3f startPos, Vector3f _dir, byte _side, ClientData _client) {
		super(_game, id, MoonbaseAssaultClientEntityCreator.GRENADE, "Grenade", playerOwnerId, _shooter, startPos, _dir, _side, _client, false, 0, 0);

		Sphere sphere = new Sphere(8, 8, 0.07f, true, false);
		sphere.setTextureMode(TextureMode.Projected);
		Geometry ball_geo = new Geometry("grenade", sphere);

		if (!_game.isServer()) { // Not running in server
			ball_geo.setShadowMode(ShadowMode.CastAndReceive);
			TextureKey key3 = new TextureKey( "Textures/grenade.png");
			Texture tex3 = game.getAssetManager().loadTexture(key3);
			Material floor_mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");
			floor_mat.setTexture("DiffuseMap", tex3);
			ball_geo.setMaterial(floor_mat);
		}

		ball_geo.setModelBound(new BoundingBox()); // Replace the BoundingSphere
		this.mainNode.attachChild(ball_geo);

		ball_geo.setUserData(Globals.ENTITY, this);
		//this.getMainNode().setUserData(Globals.ENTITY, this);

	}


	@Override
	public void processByServer(AbstractGameServer server, float tpf_secs) {
		super.processByServer(server, tpf_secs);

		if (this.checkForExploded(tpf_secs)) {
			//SmallExplosionEntity expl = new SmallExplosionEntity(server, server.getNextEntityID(), this.getWorldTranslation());
			//server.addEntity(expl);

			server.sendExplosion(this.getWorldTranslation(), 10, .8f, 1.2f, .04f, .1f, "Textures/sun.jpg");

		}
	}


	@Override
	public void processByClient(IClientApp client, float tpf_secs) {
		simpleRigidBody.process(tpf_secs);
		this.checkForExploded(tpf_secs);			
	}


	private boolean checkForExploded(float tpf_secs) {
		//Globals.p("Grenade Y:" + this.getWorldTranslation().y);
		this.timeLeft -= tpf_secs;
		if (this.timeLeft <= 0) {
			//this.remove();
			game.markForRemoval(this);
			return true;
		}
		return false;
	}


	@Override
	public float getDamageCaused() {
		return 0;
	}


	@Override
	protected void createModelAndSimpleRigidBody(Vector3f dir) {
		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this, game.getPhysicsController(), true, this);
		this.simpleRigidBody.setAerodynamicness(0.98f); // Don't roll forever
		this.simpleRigidBody.setLinearVelocity(dir.normalize().mult(10));

	}


}
