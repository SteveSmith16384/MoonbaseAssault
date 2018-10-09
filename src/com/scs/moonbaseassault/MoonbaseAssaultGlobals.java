package com.scs.moonbaseassault;

public class MoonbaseAssaultGlobals {

	// Debuggin
	public static final boolean DEBUG_NO_JUMP_ANIM = false;
	public static final boolean DEBUG_SLIDING_DOORS = false;
	public static final boolean DEBUG_OTHER_PLAYER_COLOURS = false;

	
	public static final boolean SHOW_ALL_UNITS_ON_HUD = true;

	public static final byte SIDE_ATTACKERS = 1;
	public static final byte SIDE_DEFENDERS = 2;

	public static final float STEP_FORCE = 3f;
	public static final float RAMP_FORCE = 3f;

	public static final float MAX_HEALTH = 100f;
	public static final float MOVE_SPEED = 3f;
	public static final float JUMP_FORCE = 2f;

	// Target priorities
	public static final int PRI_PLAYER = 15;
	public static final int PRI_STD_AI = 10;
	public static final int PRI_COMPUTER = 5;

	public static final int PORT = 6146;

}
