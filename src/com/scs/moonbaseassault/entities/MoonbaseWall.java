package com.scs.moonbaseassault.entities;

import java.util.HashMap;

import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.renderer.queue.RenderQueue.Bucket;
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

public class MoonbaseWall extends PhysicalEntity implements IDebrisTexture {

	private static final String INVISIBLE_TEX = "Textures/fence.png";
	private String tex;

	public MoonbaseWall(IEntityController _game, int id, float x, float yBottom, float z, float w, float h, float d, int _tex) {
		super(_game, id, MoonbaseAssaultClientEntityCreator.WALL, "Wall", false, true, false);

		tex = MATextures.getTex(_tex);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("w", w);
			creationData.put("h", h);
			creationData.put("d", d);
			creationData.put("tex", _tex);
			//creationData.put("rot", rotDegrees);
		}

		Box box1 = new Box(w/2, h/2, d/2);
		//box1.scaleTextureCoordinates(new Vector2f(w, 1)); // Don't scale vertically
		box1.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(new float[]{
				0, h, w, h, w, 0, 0, 0, // back
				0, h, d, h, d, 0, 0, 0, // right
				0, h, w, h, w, 0, 0, 0, // front
				0, h, d, h, d, 0, 0, 0, // left
				w, 0, w, d, 0, d, 0, 0, // top
				w, 0, w, d, 0, d, 0, 0  // bottom
		}));


		Geometry geometry = new Geometry("Wall", box1);
		if (!_game.isServer()) { // Not running in server
			geometry.setShadowMode(ShadowMode.CastAndReceive);

			TextureKey key3 = null;
			if (!Globals.TRANSPARENT_WALLS) {
				key3 = new TextureKey(tex);
			} else {
				key3 = new TextureKey(INVISIBLE_TEX);
			}
			key3.setGenerateMips(true);
			Texture tex3 = game.getAssetManager().loadTexture(key3);
			tex3.setWrap(WrapMode.Repeat);

			Material floor_mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");  // create a simple material
			floor_mat.setTexture("DiffuseMap", tex3);
			geometry.setMaterial(floor_mat);

			if (Globals.TRANSPARENT_WALLS) {
				floor_mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
				geometry.setQueueBucket(Bucket.Transparent);
			}

		}
		this.mainNode.attachChild(geometry);
		geometry.setLocalTranslation((w/2), h/2, (d/2)); // Never change position of mainNode (unless the whole object is moving)
		/*if (rotDegrees != 0) {
			float rads = (float)Math.toRadians(rotDegrees);
			mainNode.rotate(0, rads, 0);
		}*/
		mainNode.setLocalTranslation(x, yBottom, z);

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
