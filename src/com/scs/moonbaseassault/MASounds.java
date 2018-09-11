package com.scs.moonbaseassault;

import com.scs.stevetech1.server.Globals;

public class MASounds {

	// Sounds
	public static final int SFX_AI_KILLED = 2;
	public static final int SFX_LASER_BULLET_FIRED = 3;
	public static final int SFX_EXPLOSION = 4;
	public static final int SFX_COMPUTER_DESTROYED = 5;
	public static final int SFX_SLIDING_DOOR = 6;

	public static String getSoundFile(int id) {
		switch (id) {
		case SFX_AI_KILLED: return "";
		case SFX_LASER_BULLET_FIRED: return "Sounds/laser3.wav";
		case SFX_EXPLOSION: return "";
		case SFX_COMPUTER_DESTROYED: return "Sounds/computer_destroyed.mp3";
		case SFX_SLIDING_DOOR: return "Sounds/slidingdoor.mp3";
		default:
			if (!Globals.RELEASE_MODE) {
				throw new IllegalArgumentException("Unknown sound id:" + id);
			}
		}
		return null;
	}

}
