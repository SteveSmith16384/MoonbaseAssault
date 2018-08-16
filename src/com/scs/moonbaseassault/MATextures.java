package com.scs.moonbaseassault;

public class MATextures {

	public static final int MOONBASE_WALL = 1;
	public static final int ESCAPE_HATCH = 2;
	public static final int CORRIDOR = 3;
	public static final int MOONROCK = 4;
	public static final int FLOOR4 = 5;
	public static final int DOOR_LR = 6;
	public static final int SPACECRATE1 = 7;
	public static final int CEILING_GREEBLE = 8;


	public static String getTex(int code) {
		switch (code) {
		case MOONBASE_WALL:
			return "Textures/ufo2_03.png";
		case ESCAPE_HATCH:
			return "Textures/escape_hatch.jpg";
		case CORRIDOR:
			return "Textures/corridor.jpg";
		case MOONROCK:
			return "Textures/moonrock.png";
		case FLOOR4:
			return "Textures/floor4.jpg";
		case DOOR_LR:
			return "Textures/door_lr.png";
		case SPACECRATE1:
			return "Textures/spacecrate1.png";
		case CEILING_GREEBLE:
			return "Textures/spaceship_wall.png";
		default:
			throw new IllegalArgumentException("code");
		}
	}
}
