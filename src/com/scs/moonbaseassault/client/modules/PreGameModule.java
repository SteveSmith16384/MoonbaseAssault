package com.scs.moonbaseassault.client.modules;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.ui.Picture;
import com.scs.moonbaseassault.MoonbaseAssaultGlobals;
import com.scs.moonbaseassault.client.MoonbaseAssaultClient;
import com.scs.moonbaseassault.models.SoldierModel;
import com.scs.stevetech1.entities.AbstractAvatar;

public class PreGameModule extends AbstractModule {//implements ActionListener {

	private static final float RUNNING_SPEED = 2;

	private static ColorRGBA defaultColour = ColorRGBA.Green;

	private Node introNode;
	private float waitFor = 0;
	private SoldierModel side1Model, side2Model, playerModel;
	private BitmapText bmpTextAttacker, bmpTextDefender, bmpTextPlayer; 

	public PreGameModule(MoonbaseAssaultClient client) {
		super(client);

	}


	@Override
	public void simpleInit() {
		//super.simpleInit();
		
		BitmapFont fontSmall = client.getAssetManager().loadFont("Interface/Fonts/Console.fnt");

		BitmapText bmpText = new BitmapText(fontSmall, false);
		bmpText.setColor(defaultColour);
		bmpText.setLocalTranslation(10, client.getCamera().getHeight()-5, 0);
		client.getGuiNode().attachChild(bmpText);
		bmpText.setText("Click mouse to Start Game!");

		bmpTextAttacker = new BitmapText(fontSmall, false);
		bmpTextAttacker.setColor(defaultColour);
		bmpTextAttacker.setLocalTranslation(10, 30, 0);
		client.getGuiNode().attachChild(bmpTextAttacker);
		bmpTextAttacker.setText("Moonbase\nAttacker");

		bmpTextDefender = new BitmapText(fontSmall, false);
		bmpTextDefender.setColor(defaultColour);
		bmpTextDefender.setLocalTranslation(client.getCamera().getWidth()*.4f, 30, 0);
		client.getGuiNode().attachChild(bmpTextDefender);
		bmpTextDefender.setText("Moonbase\nDefender");

		bmpTextPlayer = new BitmapText(fontSmall, false);
		bmpTextPlayer.setColor(defaultColour);
		bmpTextPlayer.setLocalTranslation(client.getCamera().getWidth()*.8f, 30, 0);
		client.getGuiNode().attachChild(bmpTextPlayer);
		bmpTextPlayer.setText("Another\nPlayer!");

		introNode = new Node("IntroNode");

		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White.mult(1f));
		introNode.addLight(al);

		client.getCamera().setLocation(new Vector3f(0, 0.55f, 0));
		client.getCamera().lookAt(new Vector3f(10, 0.55f, 10), Vector3f.UNIT_Y);

		Vector3f lookat = client.getCamera().getLocation().clone();
		lookat.y = 0f;

		side1Model = new SoldierModel(client.getAssetManager(), MoonbaseAssaultGlobals.SIDE_ATTACKERS, false);
		side1Model.createAndGetModel();
		side1Model.setAnim(AbstractAvatar.ANIM_RUNNING);
		side1Model.getModel().setLocalTranslation(10, 0, 5);
		introNode.attachChild(side1Model.getModel());
		side1Model.getModel().lookAt(lookat, Vector3f.UNIT_Y);

		side2Model = new SoldierModel(client.getAssetManager(), MoonbaseAssaultGlobals.SIDE_DEFENDERS, false);
		side2Model.createAndGetModel();
		side2Model.setAnim(AbstractAvatar.ANIM_RUNNING);
		side2Model.getModel().setLocalTranslation(10, 0, 10);
		introNode.attachChild(side2Model.getModel());
		side2Model.getModel().lookAt(lookat, Vector3f.UNIT_Y);

		playerModel = new SoldierModel(client.getAssetManager(), MoonbaseAssaultGlobals.SIDE_DEFENDERS, true);
		playerModel.createAndGetModel();
		playerModel.setAnim(AbstractAvatar.ANIM_RUNNING);
		playerModel.getModel().setLocalTranslation(5, 0, 10);
		introNode.attachChild(playerModel.getModel());
		playerModel.getModel().lookAt(lookat, Vector3f.UNIT_Y);

		//client.getInputManager().addMapping("Ability1", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
		//client.getInputManager().addListener(this, "Ability1");

		this.client.getRootNode().attachChild(introNode);
		
		Picture logo = new Picture("Logo");//this, this.getNextEntityID(), this.getGuiNode(), "Textures/text/missionstarted.png", x, y, width, height, 3);
		logo.setImage(client.getAssetManager(), "Textures/text/ma_logo.png", true);
		int width = client.getCamera().getWidth()/2;
		logo.setWidth(width);
		int height = client.getCamera().getHeight()/2;
		logo.setHeight(height);
		int x = (client.getCamera().getWidth()/2)-(width/2);
		int y = (int)(client.getCamera().getHeight() * 0.5f);
		logo.setPosition(x, y);
		client.getGuiNode().attachChild(logo);

	}


	@Override
	public void simpleUpdate(float tpfSecs) {
		if (tpfSecs > 1) {
			tpfSecs = 1;
		}

		super.simpleUpdate(tpfSecs);
		
		if (waitFor > 0 ) {
			waitFor -= tpfSecs;
			return;
		}
				
		this.moveModel(tpfSecs, this.side1Model);
		this.moveModel(tpfSecs, this.side2Model);
		this.moveModel(tpfSecs, this.playerModel);

	}


	private void moveModel(float tpfSecs, SoldierModel model) {
		Vector3f diff = client.getCamera().getLocation().subtract(model.getModel().getLocalTranslation());
		diff.y = 0;
		if (diff.length() > 2.7f) {
			diff.normalizeLocal().multLocal(tpfSecs * RUNNING_SPEED);
			model.getModel().setLocalTranslation(model.getModel().getLocalTranslation().add(diff));
		} else {
			model.setAnim(AbstractAvatar.ANIM_IDLE);
		}
		
	}

	
	@Override
	public boolean onAction(String name, boolean value, float tpf) {
		if (name.equalsIgnoreCase("Ability1")) {
			client.startMainModule();
			return true;
		}
		return false;
	}


	@Override
	public void destroy() {
		//client.getInputManager().removeListener(this);

		this.introNode.removeFromParent();
		client.getGuiNode().detachAllChildren();
	}

}
