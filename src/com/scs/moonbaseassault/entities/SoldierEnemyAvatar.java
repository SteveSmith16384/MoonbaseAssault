package com.scs.moonbaseassault.entities;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.scs.moonbaseassault.models.SoldierModel;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.entities.AbstractOtherPlayersAvatar;
import com.scs.stevetech1.shared.IEntityController;

public class SoldierEnemyAvatar extends AbstractOtherPlayersAvatar implements AnimEventListener {
	
	private SoldierModel soldier;
	private int currentAnimCode = -1;
	
	public SoldierEnemyAvatar(IEntityController game, int type, int eid, float x, float y, float z, int side, boolean friend, String playerName) {
		super(game, type, eid, x, y, z, new SoldierModel(game.getAssetManager(), side, friend, true), side, playerName);
		
		this.soldier = (SoldierModel)anim;
	}
	

	@Override
	public void setAnimCode_ClientSide(int animCode) {
		//Globals.p("SoldierEnemyAvatar: setCurrentAnimForCode(" + s + ")");
		if (animCode != this.currentAnimCode) {
			soldier.setAnim(animCode);
		}
		this.currentAnimCode = animCode;
	}


	@Override
	public void processManualAnimation_ClientSide(float tpf_secs) {
		// Do nothing, JME handles it
	}


	@Override
	public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
		if (animName.equals("Jump")) {
			soldier.isJumping = false;
			this.currentAnimCode = AbstractAvatar.ANIM_IDLE;
		}
	}


	@Override
	public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
		// Do nothing
	}


}
