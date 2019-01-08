package com.scs.moonbaseassault.entities;

import java.util.HashMap;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.shape.Box;
import com.scs.moonbaseassault.MoonbaseAssaultGlobals;
import com.scs.simplephysics.SimpleRigidBody;
import com.scs.stevetech1.client.IClientApp;
import com.scs.stevetech1.components.IAffectedByPhysics;
import com.scs.stevetech1.components.IAnimatedClientSide;
import com.scs.stevetech1.components.IAnimatedServerSide;
import com.scs.stevetech1.components.IAvatarModel;
import com.scs.stevetech1.components.ICanShoot;
import com.scs.stevetech1.components.IDamagable;
import com.scs.stevetech1.components.IDebrisTexture;
import com.scs.stevetech1.components.IDontCollideWithComrades;
import com.scs.stevetech1.components.IDrawOnHUD;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IGetRotation;
import com.scs.stevetech1.components.IKillable;
import com.scs.stevetech1.components.INotifiedOfCollision;
import com.scs.stevetech1.components.IProcessByClient;
import com.scs.stevetech1.components.IRewindable;
import com.scs.stevetech1.components.ISetRotation;
import com.scs.stevetech1.components.ITargetableByAI;
import com.scs.stevetech1.data.SimpleGameData;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractBullet;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.jme.JMEAngleFunctions;
import com.scs.stevetech1.netmessages.EntityKilledMessage;
import com.scs.stevetech1.server.AbstractGameServer;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.server.IArtificialIntelligence;
import com.scs.stevetech1.shared.IEntityController;

public abstract class AbstractAISoldier extends PhysicalEntity implements IAffectedByPhysics, IDamagable, INotifiedOfCollision,
IRewindable, IAnimatedClientSide, IAnimatedServerSide, IDrawOnHUD, IProcessByClient, IGetRotation, ISetRotation, IKillable, ITargetableByAI, IDontCollideWithComrades,
IDebrisTexture {

	public static final int BULLETS_IN_MAG = 6;
	public static final float SHOOT_INTERVAL = .3f;
	public static final float RELOAD_INTERVAL = 4f;

	public static final float START_HEALTH = 5f;
	public static final float WALKING_SPEED = .55f; // was .53
	public static final float RUNNING_SPEED = 1.3f;//1.21f;//1.19f; //1.13f; //0.93

	private static final float HUD_DIST = 1.3f;

	private IAvatarModel soldierModel; // Need this to animate the model
	private float health = START_HEALTH;
	public byte side;
	protected IArtificialIntelligence ai;
	private int serverSideCurrentAnimCode; // Server-side
	private long timeKilled;

	// Weapon
	private int bullets = BULLETS_IN_MAG;
	public float timeToNextShotSecs = 0; 

	// HUD
	private BitmapText bmpText;
	private static BitmapFont font_small;

	public AbstractAISoldier(IEntityController _game, int id, int type, float x, float y, float z, byte _side, 
			IAvatarModel _model, String name, int startAnimCode) {
		super(_game, id, type, name, true, false, true);

		side = _side;
		soldierModel = _model; // Need it for dimensions for bb
		//this.soldierModel.createAndGetModel(); // scs new

		if (_game.isServer()) {
			creationData = new HashMap<String, Object>();
			creationData.put("side", side);
			creationData.put("name", name);
		} else {
			game.getGameNode().attachChild(this.soldierModel.createAndGetModel());
			this.setAnimCode_ClientSide(startAnimCode); // Need this since they may be dead, so we don't want to default to (say) idle
		}

		// Create box for collisions
		Box box = new Box(soldierModel.getCollisionBoxSize().x/2, soldierModel.getCollisionBoxSize().y/2, soldierModel.getCollisionBoxSize().z/2);
		Geometry bbGeom = new Geometry("bbGeom_" + name, box);
		bbGeom.setLocalTranslation(0, box.getYExtent(), 0); // origin is centre!
		bbGeom.setCullHint(CullHint.Always); // Don't draw the collision box
		bbGeom.setUserData(Globals.ENTITY, this);

		//soldierModel.getModel().getWorldBound();

		this.mainNode.attachChild(bbGeom);
		mainNode.setLocalTranslation(x, y, z);

		this.simpleRigidBody = new SimpleRigidBody<PhysicalEntity>(this, game.getPhysicsController(), game.isServer(), this);
		simpleRigidBody.canWalkUpSteps = true;
		simpleRigidBody.setBounciness(0);

		if (!_game.isServer()) {
			font_small = _game.getAssetManager().loadFont("Interface/Fonts/Console.fnt");
			bmpText = new BitmapText(font_small);
			bmpText.setText(name);
		}
	}


	@Override
	public HashMap<String, Object> getCreationData() {
		HashMap<String, Object> creationData = super.getCreationData();
		// Need this in case the soldier is dead, in which case they won't send any updates, meaning
		// they won't get sent an animation code.
		creationData.put("animcode", this.getCurrentAnimCode_ServerSide());
		return creationData;
	}


	@Override
	public void processByServer(AbstractGameServer server, float tpf_secs) {
		if (health > 0) {
			timeToNextShotSecs -= tpf_secs;
			if (server.getGameData().getGameStatus() != SimpleGameData.ST_FINISHED) {
				ai.process(server, tpf_secs);
				this.serverSideCurrentAnimCode = ai.getAnimCode();
			} else {
				this.simpleRigidBody.setAdditionalForce(Vector3f.ZERO); // Stop moving
				this.serverSideCurrentAnimCode = AbstractAvatar.ANIM_IDLE; // Game ended so we're not moving.
			}
		} else {
			this.simpleRigidBody.setAdditionalForce(Vector3f.ZERO); // Stop moving
			long diff = System.currentTimeMillis() - timeKilled;
			if (diff > 5000) {
				//this.remove();
				game.markForRemoval(this);
				return;
			}
		}

		super.processByServer(server, tpf_secs);
	}


	@Override
	public void processByClient(IClientApp client, float tpf_secs) {
		// Set position and direction of avatar model, which doesn't get moved automatically
		//if (!Globals.DEBUG_3D_PROBLEM) {
			this.soldierModel.getModel().setLocalTranslation(this.getWorldTranslation());
		//}
	}


	@Override
	public Vector3f getRotation() {
		// This is called serverside, so use the main node.
		return this.getMainNode().getLocalRotation().getRotationColumn(2);
	}


	@Override
	public void fallenOffEdge() {
		game.markForRemoval(this);
	}


	@Override
	public void damaged(float amt, IEntity collider, String reason) {
		if (Globals.DEBUG_BULLET_HIT) {
			Globals.p(this + " damaged()");
		}
		if (health > 0) {
			this.health -= amt;
			if (health <= 0) {
				if (Globals.DEBUG_BULLET_HIT) {
					Globals.p(this + " killed");
				}
				AbstractGameServer server = (AbstractGameServer)game;
				server.sendMessageToInGameClients(new EntityKilledMessage(this, collider, reason));
				this.serverSideCurrentAnimCode = AbstractAvatar.ANIM_DIED;
				this.sendUpdate = true; // Send new anim code

				this.game.getPhysicsController().removeSimpleRigidBody(this.simpleRigidBody); // Prevent us colliding
				this.simpleRigidBody.setMovedByForces(false);

				this.collideable = false;
				this.timeKilled = System.currentTimeMillis();

				server.appendToGameLog(entityName + " killed");
			}
		}
	}


	@Override
	public void remove() {
		super.remove();

		if (soldierModel.getModel() != null) {
			this.soldierModel.getModel().removeFromParent();
		}
	}


	@Override
	public byte getSide() {
		return side;
	}


	@Override
	public void notifiedOfCollision(PhysicalEntity pe) {
		if (health > 0) {
			if (game.isServer()) {
				ai.collided(pe);
			}
		}
	}


	@Override
	public void setAnimCode_ClientSide(int animCode) {
		if (Globals.USE_BOXES_FOR_AVATARS_SOLDIER) {
			return;
		}
		if (soldierModel != null) {
			this.soldierModel.setAnim(animCode);
		}
	}


	@Override
	public void processManualAnimation_ClientSide(float tpf_secs) {
		// Do nothing, already handled
	}


	/**
	 * Called server-side only,
	 */
	@Override
	public int getCurrentAnimCode_ServerSide() {
		return this.serverSideCurrentAnimCode;
	}


	@Override
	public void drawOnHud(Node hud, Camera cam) {
		if (bmpText != null) {
			if (health > 0) {
				super.checkHUDNode(hud, bmpText, cam, HUD_DIST, soldierModel.getCollisionBoxSize().y);
			}
		}
	}


	@Override
	public Node getHUDItem() {
		return this.bmpText;
	}


	@Override
	public void setRotation(Vector3f dir) {
		Vector3f dir2 = new Vector3f(dir.x, 0, dir.z); 
		JMEAngleFunctions.rotateToWorldDirection(this.soldierModel.getModel(), dir2);
	}


	@Override
	public void handleKilledOnClientSide(PhysicalEntity killer) {
		if (bmpText != null) {
			this.bmpText.removeFromParent();
		}
	}


	@Override
	public boolean isValidTargetForSide(byte shootersSide) {
		return shootersSide != this.side;
	}


	public void shoot(PhysicalEntity target) {
		if (this.timeToNextShotSecs <= 0) {
			if (Globals.DEBUG_AI_BULLET_POS) {
				Globals.p("AI shooting!  AI at " + this.getWorldTranslation());
			}
			Vector3f pos = this.getWorldTranslation().clone();
			pos.y += this.soldierModel.getBulletStartHeight();
			Vector3f dir = target.getMainNode().getWorldBound().getCenter().subtract(pos).normalizeLocal();
			AbstractBullet bullet = this.createAIBullet(pos, dir);
			this.game.addEntity(bullet);

			this.bullets--;
			if (this.bullets > 0) {
				this.timeToNextShotSecs = SHOOT_INTERVAL;
			} else {
				this.timeToNextShotSecs = RELOAD_INTERVAL;
				bullets = BULLETS_IN_MAG;
				//Globals.p("AI Reloading");
			}
		}
	}


	protected abstract AbstractBullet createAIBullet(Vector3f pos, Vector3f dir);


	@Override
	public boolean isAlive() {
		return this.health > 0;
	}


	@Override
	public float getHealth() {
		return health;
	}


	@Override
	public int getTargetPriority() {
		return MoonbaseAssaultGlobals.PRI_STD_AI;
	}


	@Override
	public boolean canBeDamaged() {
		return this.health > 0;
	}


	@Override
	public void updateClientSideHealth(int amt) {
	}

}
