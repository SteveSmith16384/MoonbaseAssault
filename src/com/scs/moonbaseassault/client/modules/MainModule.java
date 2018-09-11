package com.scs.moonbaseassault.client.modules;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.scs.moonbaseassault.MoonbaseAssaultGlobals;
import com.scs.moonbaseassault.client.MoonbaseAssaultClient;
import com.scs.moonbaseassault.entities.MA_AISoldier;
import com.scs.stevetech1.client.povweapon.DefaultPOVWeapon;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.entities.AbstractServerAvatar;
import com.scs.stevetech1.entities.PhysicalEntity;

import ssmith.util.RealtimeInterval;

public class MainModule extends AbstractModule {

	private RealtimeInterval updateHUDInterval;
	private boolean requestedToJoin = false;

	public MainModule(MoonbaseAssaultClient client) {
		super(client);

	}


	@Override
	public void simpleInit() {
		updateHUDInterval = new RealtimeInterval(2000);

		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White.mult(.6f));
		client.getGameNode().addLight(al);

		DirectionalLight sun = new DirectionalLight();
		sun.setColor(ColorRGBA.White);
		sun.setDirection(new Vector3f(.4f, -.8f, .4f).normalizeLocal());
		client.getGameNode().addLight(sun);

		// Add shadows
		final int SHADOWMAP_SIZE = 512*2;
		DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(client.getAssetManager(), SHADOWMAP_SIZE, 2);
		dlsr.setLight(sun);
		client.getViewPort().addProcessor(dlsr);

		//client.setPOVWeapon(new DefaultPOVWeapon(client));

	}


	@Override
	public void simpleUpdate(float tpfSecs) {
		if (!requestedToJoin) {
			if (client.rcvdHello) {
				requestedToJoin = true;
				client.joinGame();
			}
		}


		if (this.updateHUDInterval.hitInterval()) {
			// Get data for HUD
			List<Point> aiUnits = new LinkedList<Point>();
			List<Point> otherPlayers = new LinkedList<Point>();
			for (IEntity e : client.entities.values()) {
				if (e instanceof PhysicalEntity) {
					PhysicalEntity pe = (PhysicalEntity)e;
					if (pe instanceof MA_AISoldier) {
						MA_AISoldier ai = (MA_AISoldier)pe;
						if (ai.getSide() == client.side || MoonbaseAssaultGlobals.SHOW_ALL_UNITS_ON_HUD) {
							Vector3f pos = pe.getWorldTranslation();
							aiUnits.add(new Point((int)pos.x, (int)pos.z));
						}
					} else if (pe instanceof AbstractServerAvatar) {
						if (pe != client.currentAvatar) {
							AbstractServerAvatar ai = (AbstractServerAvatar)pe;
							if (ai.getSide() == client.side || MoonbaseAssaultGlobals.SHOW_ALL_UNITS_ON_HUD) {
								Vector3f pos = pe.getWorldTranslation();
								otherPlayers.add(new Point((int)pos.x, (int)pos.z));
							}
						}
					}
				}
			}
			Point player = null;
			if (client.currentAvatar != null) {
				Vector3f v = client.currentAvatar.getWorldTranslation();
				player = new Point((int)v.x, (int)v.z);
			}
			client.hud.setOtherData(player, aiUnits, otherPlayers);
		}
	}


	@Override
	public void mouseClicked() {

	}


	@Override
	public void destroy() {
		client.getGuiNode().detachAllChildren();
		client.getRootNode().detachAllChildren();

	}

}
