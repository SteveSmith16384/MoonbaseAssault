package com.scs.moonbaseassault.entities;

import java.util.HashMap;

import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.BufferUtils;
import com.scs.moonbaseassault.MATextures;
import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.components.IDebrisTexture;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

public class Floor extends PhysicalEntity implements IDebrisTexture {

	private String tex;

	public Floor(IEntityController _game, int id, String name, float x, float yTop, float z, float w, float h, float d, int _tex) {
		super(_game, id, MoonbaseAssaultClientEntityCreator.FLOOR, name, false, true, false);

		tex = MATextures.getTex(_tex);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("size", new Vector3f(w, h, d));
			creationData.put("tex", _tex);
			creationData.put("name", name);
		}

		Box box1 = new Box(w/2, h/2, d/2);

		box1.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(new float[]{
				0, h, w, h, w, 0, 0, 0, // back
				0, h, d, h, d, 0, 0, 0, // right
				0, h, w, h, w, 0, 0, 0, // front
				0, h, d, h, d, 0, 0, 0, // left
				w, 0, w, d, 0, d, 0, 0, // top
				w, 0, w, d, 0, d, 0, 0  // bottom
		}));

		Geometry geometry = new Geometry("FloorGeom", box1);
		if (!_game.isServer()) { // Not running in server
			geometry.setShadowMode(ShadowMode.Receive);

			TextureKey key3 = new TextureKey(tex);
			key3.setGenerateMips(true);
			Texture tex3 = game.getAssetManager().loadTexture(key3);
			tex3.setWrap(WrapMode.Repeat);

			Material floorMat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");  // create a simple material
			floorMat.setTexture("DiffuseMap", tex3);
			geometry.setMaterial(floorMat);
		}
		this.mainNode.attachChild(geometry);
		geometry.setLocalTranslation(w/2, -h/2, d/2); // Move it into position
		mainNode.setLocalTranslation(x, yTop, z); // Move it into position

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this, game.getPhysicsController(), false, this);
		simpleRigidBody.setNeverMoves(true);

		geometry.setUserData(Globals.ENTITY, this);
		mainNode.setUserData(Globals.ENTITY, this);
	}


	@Override
	public String getDebrisTexture() {
		return tex;
	}


	@Override
	public float getMinDebrisSize() {
		return 0.01f;
	}


	@Override
	public float getMaxDebrisSize() {
		return 0.04f;
	}


}
