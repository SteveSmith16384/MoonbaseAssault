package com.scs.moonbaseassault.client;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.scs.moonbaseassault.entities.AILaserBullet;
import com.scs.moonbaseassault.entities.Computer;
import com.scs.moonbaseassault.entities.DestroyedComputer;
import com.scs.moonbaseassault.entities.Floor;
import com.scs.moonbaseassault.entities.FlyingSpaceship2;
import com.scs.moonbaseassault.entities.GasCannister;
import com.scs.moonbaseassault.entities.GenericFloorTex;
import com.scs.moonbaseassault.entities.MA_AISoldier;
import com.scs.moonbaseassault.entities.MapBorder;
import com.scs.moonbaseassault.entities.MoonbaseWall;
import com.scs.moonbaseassault.entities.PlayersLaserBullet;
import com.scs.moonbaseassault.entities.PlayersGrenade;
import com.scs.moonbaseassault.entities.SlidingDoor;
import com.scs.moonbaseassault.entities.SoldierClientAvatar;
import com.scs.moonbaseassault.entities.SoldierEnemyAvatar;
import com.scs.moonbaseassault.entities.SpaceCrate;
import com.scs.moonbaseassault.entities.Spaceship1;
import com.scs.moonbaseassault.weapons.GrenadeLauncher;
import com.scs.moonbaseassault.weapons.LaserRifle;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.components.IEntityContainer;
import com.scs.stevetech1.entities.AbstractClientAvatar;
import com.scs.stevetech1.entities.AbstractEnemyAvatar;
import com.scs.stevetech1.entities.AbstractPlayersBullet;
import com.scs.stevetech1.entities.BulletTrail;
import com.scs.stevetech1.entities.DebuggingSphere;
import com.scs.stevetech1.entities.ExplosionShard;
import com.scs.stevetech1.netmessages.NewEntityData;
import com.scs.stevetech1.server.Globals;
import com.scs.stevetech1.weapons.HitscanRifle;

public class MoonbaseAssaultClientEntityCreator {

	public static final int SOLDIER_AVATAR = 1;
	public static final int COMPUTER = 2;
	public static final int FLOOR = 3;
	public static final int DOOR = 4;
	public static final int CRATE = 5;
	public static final int WALL = 6;
	public static final int PLAYER_LASER_BULLET = 7;
	public static final int LASER_RIFLE = 8;
	public static final int SPACESHIP1 = 9;
	public static final int AI_SOLDIER = 10;
	public static final int MAP_BORDER = 11;
	public static final int DESTROYED_COMPUTER = 12;
	public static final int GRENADE = 13;
	public static final int GRENADE_LAUNCHER = 14;
	//public static final int SMALL_EXPLOSION_EFFECT = 15;
	public static final int AI_LASER_BULLET = 17;
	public static final int HITSCAN_RIFLE = 18;
	public static final int GAS_CANNISTER = 21;
	public static final int FLOOR_TEX = 22;
	public static final int FLYING_SPACESHIP2 = 23;


	public MoonbaseAssaultClientEntityCreator() {
	}


	public static String TypeToString(int type) {
		switch (type) {
		case SOLDIER_AVATAR: return "Avatar";
		case COMPUTER: return "COMPUTER";
		case FLOOR: return "FLOOR";
		case DOOR: return "DOOR";
		case CRATE: return "CRATE";
		case WALL: return "WALL";
		case PLAYER_LASER_BULLET: return "PLAYER_LASER_BULLET";
		case LASER_RIFLE: return "LASER_RIFLE";
		case SPACESHIP1: return "SPACESHIP1";
		case MAP_BORDER: return "INVISIBLE_MAP_BORDER";
		case AI_LASER_BULLET: return "AI_LASER_BULLET";
		default: return "Unknown (" + type + ")";
		}
	}


	public IEntity createEntity(AbstractGameClient game, NewEntityData msg) {
		/*if (Globals.DEBUG_ENTITY_ADD_REMOVE) {
			Globals.p("Creating " + TypeToString(msg.type));
		}*/
		int id = msg.entityID;
		Vector3f pos = (Vector3f)msg.data.get("pos");

		switch (msg.type) {
		case SOLDIER_AVATAR:
		{
			int playerID = (int)msg.data.get("playerID");
			int side = (int)msg.data.get("side");
			float moveSpeed = (float)msg.data.get("moveSpeed");
			float jumpForce = (float)msg.data.get("jumpForce");
			String playersName = (String)msg.data.get("playersName");

			if (playerID == game.playerID) {
				AbstractClientAvatar avatar = new SoldierClientAvatar(game, id, game.input, game.getCamera(), game.hud, id, pos.x, pos.y, pos.z, side, moveSpeed, jumpForce);
				//game.getCamera().lookAt(pos.add(Vector3f.UNIT_X), Vector3f.UNIT_Y); // Look somewhere
				Vector3f look = new Vector3f(15f, 1f, 15f);
				game.getCamera().lookAt(look, Vector3f.UNIT_Y); // Look somewhere
				return avatar;
			} else {
				// Create a simple avatar since we don't control these
				AbstractEnemyAvatar avatar = new SoldierEnemyAvatar(game, SOLDIER_AVATAR, id, pos.x, pos.y, pos.z, side, playersName);
				return avatar;
			}
		}

		case FLOOR:
		{
			Vector3f size = (Vector3f)msg.data.get("size");
			String name = (String)msg.data.get("name");
			String tex = (String)msg.data.get("tex");
			Floor floor = new Floor(game, id, name, pos.x, pos.y, pos.z, size.x, size.y, size.z, tex);
			return floor;
		}

		case WALL:
		{
			float w = (float)msg.data.get("w");
			float h = (float)msg.data.get("h");
			float d = (float)msg.data.get("d");
			String tex = (String)msg.data.get("tex");
			//float rot = (Float)msg.data.get("rot");
			MoonbaseWall wall = new MoonbaseWall(game, id, pos.x, pos.y, pos.z, w, h, d, tex);
			return wall;
		}

		case LASER_RIFLE:
		{
			int ownerid = (int)msg.data.get("ownerid");
			int num = (int)msg.data.get("num");
			int playerID = (int)msg.data.get("playerID");
			LaserRifle gl = new LaserRifle(game, id, playerID, null, ownerid, num, null);
			return gl;
		}

		case PLAYER_LASER_BULLET:
		{
			int containerID = (int) msg.data.get("containerID");
			int playerID = (int) msg.data.get("playerID");
			int side = (int) msg.data.get("side");
			Vector3f dir = (Vector3f) msg.data.get("dir");
			IEntityContainer<AbstractPlayersBullet> irac = (IEntityContainer<AbstractPlayersBullet>)game.entities.get(containerID);
			PlayersLaserBullet bullet = new PlayersLaserBullet(game, id, playerID, irac, side, null, dir);
			return bullet;
		}

		case DOOR:
		{
			float w = (float)msg.data.get("w");
			float h = (float)msg.data.get("h");
			String tex = (String)msg.data.get("tex");
			float rot = (Float)msg.data.get("rot");
			SlidingDoor wall = new SlidingDoor(game, id, pos.x, pos.y, pos.z, w, h, tex, rot);
			return wall;
		}

		case COMPUTER:
		{
			Computer computer = new Computer(game, id, pos.x, pos.y, pos.z, -1, -1);
			return computer;
		}

		case DESTROYED_COMPUTER:
		{
			DestroyedComputer dcomputer = new DestroyedComputer(game, id, pos.x, pos.y, pos.z);
			return dcomputer;
		}

		case SPACESHIP1:
		{
			Quaternion q = (Quaternion)msg.data.get("quat");
			Spaceship1 spaceship1 = new Spaceship1(game, id, pos.x, pos.y, pos.z, q);
			return spaceship1;
		}

		case AI_SOLDIER:
		{
			int side = (int)msg.data.get("side");
			int animcode = (int)msg.data.get("animcode");
			String name = (String)msg.data.get("name");
			MA_AISoldier z = new MA_AISoldier(game, id, pos.x, pos.y, pos.z, side, animcode, name);
			return z;
		}

		case MAP_BORDER:
		{
			Vector3f dir = (Vector3f)msg.data.get("dir");
			float size = (float)msg.data.get("size");
			MapBorder hill = new MapBorder(game, id, pos.x, pos.y, pos.z, size, dir);
			return hill;
		}

		case GRENADE_LAUNCHER: 
		{
			int ownerid = (int)msg.data.get("ownerid");
			//if (game.currentAvatar != null) { // We might not have an avatar yet
			//	if (ownerid == game.currentAvatar.id) { // Don't care about other's abilities?
			//AbstractAvatar owner = (AbstractAvatar)game.entities.get(ownerid);
			int num = (int)msg.data.get("num");
			int playerID = (int)msg.data.get("playerID");
			GrenadeLauncher gl = new GrenadeLauncher(game, id, playerID, null, ownerid, num, null);
			return gl;
			//	}
			//}
			//return null;
		}

		case GRENADE:
		{
			int containerID = (int) msg.data.get("containerID");
			int playerID = (int) msg.data.get("playerID");
			int side = (int) msg.data.get("side");
			IEntityContainer<AbstractPlayersBullet> irac = (IEntityContainer<AbstractPlayersBullet>)game.entities.get(containerID);
			PlayersGrenade snowball = new PlayersGrenade(game, id, playerID, irac, side, null);
			return snowball;
		}

		case CRATE:
		{
			Vector3f size = (Vector3f)msg.data.get("size");
			String tex = (String)msg.data.get("tex");
			float rot = (float)msg.data.get("rot");
			SpaceCrate crate = new SpaceCrate(game, id, pos.x, pos.y, pos.z, size.x, size.y, size.z, tex, rot);
			return crate;
		}

		/*case SMALL_EXPLOSION_EFFECT:
		{
			SmallExplosionEntity expl = new SmallExplosionEntity(game, id, pos);
			return expl;
		}*/

		case Globals.DEBUGGING_SPHERE:
		{
			DebuggingSphere hill = new DebuggingSphere(game, id, pos.x, pos.y, pos.z, true, false);
			return hill;
		}

		case HITSCAN_RIFLE:
		{
			int playerID = (int) msg.data.get("playerID");
			int ownerid = (int)msg.data.get("ownerid");
			//if (ownerid == game.currentAvatar.id) { // Don't care about other's abilities
			//AbstractAvatar owner = (AbstractAvatar)game.entities.get(ownerid);
			int num = (int)msg.data.get("num");
			HitscanRifle gl = new HitscanRifle(game, id, HITSCAN_RIFLE, playerID, null, ownerid, num, null);
			//owner.addAbility(gl, num);
			return gl;
			//}
			//return null;
		}

		case Globals.BULLET_TRAIL:
		{
			int playerID = (int) msg.data.get("playerID");
			if (playerID != game.getPlayerID()) {
				Vector3f start = (Vector3f) msg.data.get("start");
				Vector3f end = (Vector3f) msg.data.get("end");
				BulletTrail bullet = new BulletTrail(game, playerID, start, end);
				return bullet;
			} else {
				return null; // We create our own bullet trails, so ignore this
			}

		}

		case AI_LASER_BULLET:
		{
			int side = (int) msg.data.get("side");
			int shooterID = (int) msg.data.get("shooterID");
			IEntity shooter = game.entities.get(shooterID);
			Vector3f dir = (Vector3f) msg.data.get("dir");
			AILaserBullet bullet = new AILaserBullet(game, id, side, pos.x, pos.y, pos.z, shooter, dir);
			return bullet;
		}

		case Globals.EXPLOSION_SHARD:
		{
			Vector3f forceDirection = (Vector3f) msg.data.get("forceDirection");
			float size = (float) msg.data.get("size");
			String tex = (String) msg.data.get("tex");
			ExplosionShard expl = new ExplosionShard(game, pos.x, pos.y, pos.z, size, forceDirection, tex);
			return expl;
		}

		case GAS_CANNISTER:
		{
			GasCannister gas = new GasCannister(game, id, pos.x, pos.y, pos.z);
			return gas;
		}

		case FLOOR_TEX:
		{
			Vector3f size = (Vector3f)msg.data.get("size");
			String tex = (String)msg.data.get("tex");
			GenericFloorTex gas = new GenericFloorTex(game, id, pos.x, pos.y, pos.z, size.x, size.z, tex);
			return gas;
		}

		case FLYING_SPACESHIP2:
		{
			FlyingSpaceship2 spaceship1 = new FlyingSpaceship2(game, id, pos.x, pos.y, pos.z);
			return spaceship1;
		}

		default:
			throw new RuntimeException("Unknown entity type for creation: " + msg.type);
		}
	}

}