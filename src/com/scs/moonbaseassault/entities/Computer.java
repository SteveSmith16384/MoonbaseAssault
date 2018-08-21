package com.scs.moonbaseassault.entities;

import com.jme3.texture.Image;
import java.awt.Point;
import java.util.HashMap;

import com.jme3.asset.TextureKey;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.scs.moonbaseassault.MoonbaseAssaultGlobals;
import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.moonbaseassault.server.MoonbaseAssaultServer;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.components.IDamagable;
import com.scs.stevetech1.components.IDebrisTexture;
import com.scs.stevetech1.components.IDrawOnHUD;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.ITargetable;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.hud.IHUD;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.shared.IEntityController;

public class Computer extends PhysicalEntity implements IDamagable, ITargetable, IDrawOnHUD, IDebrisTexture {

	private static final float SIZE = 0.9f;
	private float health = 100;
	private Point position; // Server-side only

	// HUD
	private BitmapText hudNode;
	private static BitmapFont font_small;

	public Computer(IEntityController _game, int id, float x, float y, float z, int mx, int my) {
		super(_game, id, MoonbaseAssaultClientEntityCreator.COMPUTER, "Computer", true, true, false); // Requires processing so it can be a target

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			position = new Point(mx, my);
		}

		float w = SIZE;
		float h = SIZE;
		float d = SIZE;
		Box box1 = new Box(w/2, h/2, d/2);

		Geometry geometry = new Geometry("Computer", box1);
		if (!_game.isServer()) {
			geometry.setShadowMode(ShadowMode.CastAndReceive);

			//TextureKey key = new TextureKey("Textures/computerconsole2.jpg");
			TextureKey key = new TextureKey("Textures/computer_speccy.jpg");
			key.setGenerateMips(true);
			Texture tex = game.getAssetManager().loadTexture(key);
			
			tex.setWrap(WrapMode.Repeat);
			Material floorMat = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");  // create a simple material
			floorMat.setTexture("DiffuseMap", tex);
			geometry.setMaterial(floorMat);
		}
		this.mainNode.attachChild(geometry);
		geometry.setLocalTranslation(w/2, h/2, d/2);
		mainNode.setLocalTranslation(x, y, z);

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this, game.getPhysicsController(), false, this);
		simpleRigidBody.setNeverMoves(true);

		geometry.setUserData(Globals.ENTITY, this);
		mainNode.setUserData(Globals.ENTITY, this);

		if (!_game.isServer()) {
			font_small = _game.getAssetManager().loadFont("Interface/Fonts/Console.fnt");
			hudNode = new BitmapText(font_small);
			hudNode.setText((int)this.health + "%");
		}
	}


	@Override
	public void damaged(float amt, IEntity collider, String reason) {
		if (this.health > 0) {
			//Globals.p("Computer hit!");
			this.health -= amt;
			if (this.health <= 0) {
				health = 0;
				MoonbaseAssaultServer server = (MoonbaseAssaultServer)game;
				server.computerDestroyed(position);

				this.remove();

				server.sendExplosion(this.getWorldTranslation(), 10, .8f, 1.2f, .06f, .12f, "Textures/computerconsole2.jpg");
				game.playSound("Sounds/computer_destroyed.mp3", this.getWorldTranslation(), Globals.DEF_VOL, false);

				Vector3f pos = this.getWorldTranslation();
				DestroyedComputer dc = new DestroyedComputer(game, game.getNextEntityID(), pos.x, pos.y, pos.z);
				game.addEntity(dc);
			} else {
				this.sendUpdate = true; // Send new health
			}
		}
	}


	@Override
	public int getSide() {
		return 2;
	}


	@Override
	public float getHealth() {
		return health;
	}


	@Override
	public boolean isValidTargetForSide(int shootersSide) {
		return shootersSide == 1;
	}


	@Override
	public boolean isAlive() {
		return true;
	}


	@Override
	public Node getHUDItem() {
		return this.hudNode;
	}


	@Override
	public void drawOnHud(IHUD hud, Camera cam) {
		/*float dist = this.getWorldTranslation().distance(cam.getLocation());
		if (dist < 3f) {
			FrustumIntersect insideoutside = cam.contains(this.getMainNode().getWorldBound());
			if (insideoutside != FrustumIntersect.Outside) {
				if (this.hudNode.getText().length() == 0) {
					hudNode.setText((int)this.health + "%");
				}
				Vector3f pos = this.getWorldTranslation().clone();
				pos.y += SIZE;
				Vector3f screen_pos = cam.getScreenCoordinates(pos);
				this.hudNode.setLocalTranslation(screen_pos.x, screen_pos.y, 0);
			}
		} else {
			if (this.hudNode.getText().length() > 0) {
				this.hudNode.setText(""); // Hide it
			}
		}*/
		super.checkHUDNode(hudNode, hud, cam, 3f, SIZE);

	}


	@Override
	public String getDebrisTexture() {
		return "Textures/computerconsole2.jpg";
	}


	@Override
	public int getTargetPriority() {
		return MoonbaseAssaultGlobals.PRI_COMPUTER;
	}


	@Override
	public float getMinDebrisSize() {
		return 0.01f;
	}


	@Override
	public float getMaxDebrisSize() {
		return 0.04f;
	}


	@Override
	public void updateClientSideHealth(final int amt) {
		hudNode.setText((int)this.health + "%");
	}


}
