package com.scs.moonbaseassault;

import com.jme3.network.serializing.Serializable;
import com.scs.stevetech1.data.SimplePlayerData;

@Serializable
public class MASimplePlayerData extends SimplePlayerData {

	public int score;
	
	public MASimplePlayerData() {
		super();
	}
	
}

