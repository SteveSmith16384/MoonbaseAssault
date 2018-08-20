package com.scs.moonbaseassault.client.modules;

import java.io.IOException;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.controls.ActionListener;
import com.scs.moonbaseassault.client.MoonbaseAssaultClient;

public class DisconnectedModule extends AbstractModule implements ActionListener {
	
	private BitmapText bmpText;
	
	public DisconnectedModule(MoonbaseAssaultClient client) {
		super(client);

	}

	
	@Override
	public void simpleInit() {
		super.simpleInit();
		
		BitmapFont font_small = client.getAssetManager().loadFont("Interface/Fonts/Console.fnt");

		bmpText = new BitmapText(font_small, false);
		bmpText.setColor(IntroModule.defaultColour);
		bmpText.setLocalTranslation(10, 100, 0);
		client.getGuiNode().attachChild(bmpText);
		bmpText.setText("Disconnected!  Click to attempt reconnection...");

		client.getInputManager().addListener(this, "Ability1");
	}
	

	@Override
	public void simpleUpdate(float tpfSecs) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onAction(String name, boolean value, float tpf) {
		if (name.equalsIgnoreCase("Ability1")) {
			client.startConnectToServerModule();
		}		
	}


	@Override
	public void destroy() {
		client.getInputManager().removeListener(this);
		client.getGuiNode().detachAllChildren();
	}


}
