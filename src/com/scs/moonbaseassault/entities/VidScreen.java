package com.scs.moonbaseassault.entities;

import java.util.HashMap;

import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.scs.moonbaseassault.MATextures;
import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

public class VidScreen extends PhysicalEntity {//implements IDebrisTexture {

	private String tex;

	public VidScreen(IEntityController _game, int id, float x, float yBottom, float z, float w, float h, float d, int _tex) {
		super(_game, id, MoonbaseAssaultClientEntityCreator.VID_SCREEN, "VidScreen", true, true, false);

		tex = MATextures.getTex(_tex);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("w", w);
			creationData.put("h", h);
			creationData.put("d", d);
			creationData.put("tex", _tex);
		}

		Box box1 = new Box(w/2, h/2, d/2);
		box1.scaleTextureCoordinates(new Vector2f(1, 1));

		Geometry geometry = new Geometry("VidScreen", box1);
		if (!_game.isServer()) { // Not running in server
			//geometry.setShadowMode(ShadowMode.CastAndReceive);

			TextureKey key3 = null;
			key3 = new TextureKey(tex, false);
			key3.setGenerateMips(true);
			Texture tex3 = game.getAssetManager().loadTexture(key3);
			tex3.setWrap(WrapMode.Repeat);

			Material mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");  // create a simple material
			mat.setTexture("DiffuseMap", tex3);
			geometry.setMaterial(mat);

		}
		this.mainNode.attachChild(geometry);
		geometry.setLocalTranslation((w/2), h/2, (d/2)); // Never change position of mainNode (unless the whole object is moving)
		mainNode.setLocalTranslation(x, yBottom, z);

		//this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this, game.getPhysicsController(), false, this);
		//simpleRigidBody.setNeverMoves(true);

		geometry.setUserData(Globals.ENTITY, this);
	}

/*
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

	*/
}
