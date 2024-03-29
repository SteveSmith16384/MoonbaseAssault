package com.scs.moonbaseassault.entities;

import java.util.HashMap;

import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.client.IClientApp;
import com.scs.stevetech1.components.IPlayerCollectable;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.jme.JMEModelFunctions;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

public class MediPack extends PhysicalEntity implements IProcessByClient, IPlayerCollectable {

	private float rotDegrees = 0;
	private Geometry geometry;
	
	public MediPack(IEntityController _game, int id, float x, float y, float z) {
		super(_game, id, MoonbaseAssaultClientEntityCreator.MEDIPACK, "MediPack", true, false, false);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
		}
		
		float w = .2f;
		float h = .2f;
		float d = .1f;

		Box box1 = new Box(w/2, h/2, d/2);

		geometry = new Geometry("MediPack", box1);
		if (!_game.isServer()) {
			geometry.setShadowMode(ShadowMode.CastAndReceive);

			TextureKey key3 = new TextureKey("Textures/redcross.png");
			key3.setGenerateMips(true);
			Texture tex3 = game.getAssetManager().loadTexture(key3);
			tex3.setWrap(WrapMode.Repeat);

			Material mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");  // create a simple material
			mat.setTexture("DiffuseMap", tex3);
			geometry.setMaterial(mat);
		}
		JMEModelFunctions.moveYOriginTo(geometry, 0.01f); //was 0.05 Floating

		this.mainNode.attachChild(geometry);
		mainNode.setLocalTranslation(x, y, z);
		mainNode.updateModelBound();

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this, game.getPhysicsController(), false, this);
		simpleRigidBody.setNeverMoves(true);
		simpleRigidBody.setSolid(false);

		geometry.setUserData(Globals.ENTITY, this);
		
	}


	@Override
	public void processByClient(IClientApp client, float tpfSecs) {
		/*rotDegrees += (tpfSecs * 0.05f);
		if (rotDegrees > 360) {
			rotDegrees -= 360;
		}*/
		float rads = (float)Math.toRadians((tpfSecs * 0.05f));
		geometry.rotate(0, rads, 0);

	}


	@Override
	public void collected(AbstractServerAvatar avatar) {
		if (avatar.getHealth() < avatar.getMaxHealth()) {
			game.markForRemoval(this);
			avatar.setHealth(avatar.getMaxHealth());
			avatar.sendAvatarStatusUpdateMessage(false, true);

		}
	}

}
