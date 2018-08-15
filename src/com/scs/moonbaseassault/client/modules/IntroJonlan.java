package com.scs.moonbaseassault.client.modules;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.light.AmbientLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.scs.moonbaseassault.client.MoonbaseAssaultClient;
import com.scs.moonbaseassault.models.SoldierModel;
import com.scs.stevetech1.entities.AbstractAvatar;
import com.scs.stevetech1.input.SimpleMouseInput;

public class IntroJonlan extends AbstractModule {

	private static final int STAGE_JONLAN = 0;
	private static final int STAGE_REGNIX = 1;
	
	private static final float RUNNING_SPEED = 2;

	private static ColorRGBA defaultColour = ColorRGBA.Green;

	private Node introNode;
	private float waitFor = 0;
	private int currentStage = STAGE_JONLAN;
	private SoldierModel jonlanModel;
	private BitmapText text;

	public IntroJonlan(MoonbaseAssaultClient client) {
		super(client);

	}


	@Override
	public void simpleInit() {
		BitmapFont fontSmall = client.getAssetManager().loadFont("Interface/Fonts/Console.fnt");

		BitmapText bmpText = new BitmapText(fontSmall, false);
		bmpText.setColor(defaultColour);
		bmpText.setLocalTranslation(10, 10, 0);
		client.getGuiNode().attachChild(bmpText);
		bmpText.setText("Click mouse to Start");

		text = new BitmapText(fontSmall, false);
		text.setColor(defaultColour);
		text.setLocalTranslation(client.getCamera().getWidth()/2, client.getCamera().getHeight()-100, 0);
		client.getGuiNode().attachChild(text);
		text.setText("Starring...");

		introNode = new Node("IntroNode");

		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White.mult(1f));
		introNode.addLight(al);

		client.getCamera().setLocation(new Vector3f(0, 0.55f, 0));
		client.getCamera().lookAt(new Vector3f(10, 0.55f, 10), Vector3f.UNIT_Y);

		jonlanModel = new SoldierModel(client.getAssetManager());
		jonlanModel.createAndGetModel(1);
		jonlanModel.setAnim(AbstractAvatar.ANIM_RUNNING);
		jonlanModel.getModel().setLocalTranslation(10, 0, 5);
		introNode.attachChild(jonlanModel.getModel());

		Vector3f lookat = client.getCamera().getLocation().clone();
		lookat.y = 0f;
		jonlanModel.getModel().lookAt(lookat, Vector3f.UNIT_Y);

		client.input = new SimpleMouseInput(client.getInputManager());

		this.client.getRootNode().attachChild(introNode);
	}


	@Override
	public void simpleUpdate(float tpfSecs) {
		if (tpfSecs > 1) {
			tpfSecs = 1;
		}

		if (waitFor > 0 ) {
			waitFor -= tpfSecs;
			return;
		}

		switch (this.currentStage) {
		case STAGE_JONLAN:
			Vector3f diff = client.getCamera().getLocation().subtract(jonlanModel.getModel().getLocalTranslation());
			diff.y = 0;
			if (diff.length() > .7f) {
				diff.normalizeLocal().multLocal(tpfSecs * RUNNING_SPEED);
				jonlanModel.getModel().setLocalTranslation(jonlanModel.getModel().getLocalTranslation().add(diff));
			} else {
				jonlanModel.setAnim(AbstractAvatar.ANIM_IDLE);
				text.setText("Starring\nCORPORAL JONLAN");
				this.waitFor = 2;
				this.currentStage = STAGE_REGNIX;
			}
			break;
		case STAGE_REGNIX:
			break;
		}


		if (this.client.input.isAbilityPressed(1)) {
			client.startConnectToServerModule();
			return;
		}
	}



	@Override
	public void destroy() {
		this.introNode.removeFromParent();
		client.getGuiNode().detachAllChildren();
	}


}
