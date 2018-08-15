package com.scs.moonbaseassault.entities;

import com.jme3.renderer.Camera;
import com.scs.moonbaseassault.MoonbaseAssaultGlobals;
import com.scs.moonbaseassault.client.MoonbaseAssaultClientEntityCreator;
import com.scs.moonbaseassault.models.SoldierModel;
import com.scs.stevetech1.avatartypes.PersonAvatar;
import com.scs.stevetech1.client.AbstractGameClient;
import com.scs.stevetech1.entities.AbstractClientAvatar;
import com.scs.stevetech1.hud.IHUD;
import com.scs.stevetech1.input.IInputDevice;

public class SoldierClientAvatar extends AbstractClientAvatar {

	public SoldierClientAvatar(AbstractGameClient _module, int _playerID, IInputDevice _input, Camera _cam, IHUD _hud, int eid, float x, float y, float z, int side) {
		super(_module, MoonbaseAssaultClientEntityCreator.SOLDIER_AVATAR, _playerID, _input, _cam, _hud, eid, x, y, z, side, new SoldierModel(_module.getAssetManager(), side, true), new PersonAvatar(_module, _input, MoonbaseAssaultGlobals.MOVE_SPEED, MoonbaseAssaultGlobals.JUMP_FORCE));
	}

}
