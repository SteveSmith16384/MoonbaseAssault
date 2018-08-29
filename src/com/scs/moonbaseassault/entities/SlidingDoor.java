package com.scs.moonbaseassault.entities;

import java.util.HashMap;

import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.scs.moonbaseassault.MASounds;
import com.scs.moonbaseassault.MATextures;
import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.moonbaseassault.server.MoonbaseAssaultServer;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.components.INotifiedOfCollision;
import com.scs.stevetech1.components.IProcessByServer;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

/*
 * origin is the bottom.
 */
public class SlidingDoor extends PhysicalEntity implements INotifiedOfCollision, IProcessByServer {

	private static final Vector3f MOVE_UP = new Vector3f(0, 1f, 0);
	private static final float STAY_OPEN_DURATION = 3f;

	private Vector3f origPosition;
	private boolean isOpening = false;
	private float timeUntilClose;

	public SlidingDoor(IEntityController _game, int id, float x, float yBottom, float z, float w, float h, int tex, float rotDegrees) {
		super(_game, id, MoonbaseAssaultClientEntityCreator.DOOR, "SlidingDoor", true, true, true);

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("w", w);
			creationData.put("h", h);
			creationData.put("tex", tex);
			creationData.put("rot", rotDegrees);
		}

		float depth = 0.1f; // Default thickness

		Box box1 = new Box(w/2, h/2, depth/2);
		box1.scaleTextureCoordinates(new Vector2f(w, 1)); // Don't scale vertically
		Geometry geometry = new Geometry("SlidingDoor", box1);
		if (!_game.isServer()) { // Not running in server
			geometry.setShadowMode(ShadowMode.CastAndReceive);

			TextureKey key3 = new TextureKey(MATextures.getTex(tex));
			key3.setGenerateMips(true);
			Texture tex3 = game.getAssetManager().loadTexture(key3);
			tex3.setWrap(WrapMode.Repeat);

			Material floor_mat = null;
				floor_mat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");  // create a simple material
				floor_mat.setTexture("DiffuseMap", tex3);
			geometry.setMaterial(floor_mat);
		}
		this.mainNode.attachChild(geometry);
		if (rotDegrees != 0) {
			float rads = (float)Math.toRadians(rotDegrees);
			mainNode.rotate(0, rads, 0);
		}
		geometry.setLocalTranslation(w/2, h/2, depth/2 + (w/2)); // Never change position of mainNode (unless the whole object is moving)
		mainNode.setLocalTranslation(x, yBottom, z);

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this, game.getPhysicsController(), false, this);
		simpleRigidBody.setGravity(0f);

		geometry.setUserData(Globals.ENTITY, this);
		mainNode.setUserData(Globals.ENTITY, this);

		origPosition = this.getWorldTranslation().clone();

	}


	@Override
	public void processByServer(AbstractGameServer server, float tpf_secs) {
		super.processByServer(server, tpf_secs);

		if (this.isOpening) {
			float topPos = MoonbaseAssaultServer.CEILING_HEIGHT-.1f;
			if (this.getWorldTranslation().y < topPos) {
				this.adjustWorldTranslation(MOVE_UP.mult(tpf_secs));
				//this.getMainNode().move(MOVE_UP.mult(tpf_secs));
				// position accurately at top in case of large jump
				if (this.getWorldTranslation().y > topPos) {
					this.getWorldTranslation().y = topPos;
				}
				if (Globals.DEBUG_SLIDING_DOORS) {
					Globals.p("Door is opening");
				}
			} else {
				this.isOpening = false;
			}
		} else {
			if (timeUntilClose <= 0) {
				if (this.getWorldTranslation().y > 0) {
					this.adjustWorldTranslation(MOVE_UP.mult(tpf_secs).mult(-1));
					// position accurately at top in case of large jump
					if (this.getWorldTranslation().y < 0) {
						this.getWorldTranslation().y = 0;
					}
					if (!this.simpleRigidBody.checkForCollisions(true).isEmpty()) { //todo - need this?
						// Move back up
						this.adjustWorldTranslation(MOVE_UP.mult(tpf_secs));
						this.startOpening();
					} else {
						if (Globals.DEBUG_SLIDING_DOORS) {
							Globals.p("Door is closing");
						}
					}
				}
			} else {
				timeUntilClose -= tpf_secs;
			}
		}
	}


	@Override
	public void collided(PhysicalEntity pe) {
		this.startOpening();
	}


	private void startOpening() {
		this.isOpening = true;
		timeUntilClose = STAY_OPEN_DURATION;

		game.playSound(MASounds.SFX_SLIDING_DOOR, this.getID(), getWorldTranslation(), Globals.DEF_VOL, false);
	}


}
