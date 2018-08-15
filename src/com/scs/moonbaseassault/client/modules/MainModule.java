package com.scs.moonbaseassault.client.modules;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.scs.moonbaseassault.client.MoonbaseAssaultClient;
import com.scs.moonbaseassault.entities.Computer;
import com.scs.moonbaseassault.entities.MA_AISoldier;
import com.scs.stevetech1.components.IEntity;
import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.server.Globals;

import ssmith.util.RealtimeInterval;

public class MainModule extends AbstractModule {

	private RealtimeInterval updateHUDInterval;

	public MainModule(MoonbaseAssaultClient client) {
		super(client);

	}


	@Override
	public void simpleInit() {
		this.client.setupForGame();

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

	}


	@Override
	public void simpleUpdate(float tpfSecs) {
		if (this.updateHUDInterval.hitInterval()) {
			// Get data for HUD
			List<Point> units = new LinkedList<Point>();
			List<Point> computers = new LinkedList<Point>();
			for (IEntity e : client.entities.values()) {
				if (e instanceof PhysicalEntity) {
					PhysicalEntity pe = (PhysicalEntity)e;  //pe.getWorldRotation();
					if (pe instanceof Computer) {
						Vector3f pos = pe.getWorldTranslation();
						computers.add(new Point((int)pos.x, (int)pos.z));
					} else if (pe instanceof MA_AISoldier) {
						MA_AISoldier ai = (MA_AISoldier)pe;
						if (ai.getSide() == client.side || Globals.SHOW_ALL_UNITS_ON_HUD) { // Only show our side
							Vector3f pos = pe.getWorldTranslation();
							units.add(new Point((int)pos.x, (int)pos.z));
						}
					}
				}
			}
			Point player = null;
			if (client.currentAvatar != null) {
				Vector3f v = client.currentAvatar.getWorldTranslation();
				player = new Point((int)v.x, (int)v.z);
			}
			//this.hud.hudMapImage.mapImageTex.setOtherData(player, units, computers);
			client.hud.setOtherData(player, units, computers);
		}
	}


	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

}
