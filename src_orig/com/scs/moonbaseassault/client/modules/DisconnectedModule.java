package com.scs.moonbaseassault.client.modules;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.scs.moonbaseassault.client.MoonbaseAssaultClient;

public class DisconnectedModule extends AbstractModule {
	
	private BitmapText bmpText;
	
	public DisconnectedModule(MoonbaseAssaultClient client) {
		super(client);

	}

	
	@Override
	public void simpleInit() {
		BitmapFont font_small = client.getAssetManager().loadFont("Interface/Fonts/Console.fnt");

		bmpText = new BitmapText(font_small, false);
		bmpText.setColor(IntroModule.defaultColour);
		bmpText.setLocalTranslation(10, 100, 0);
		client.getGuiNode().attachChild(bmpText);
		bmpText.setText("Disconnected!  Click to Exit");

	}
	

	@Override
	public void simpleUpdate(float tpfSecs) {
		super.simpleUpdate(tpfSecs);
		
	}


	@Override
	public void mouseClicked() {
		client.quit("Disconnected");
	}


	@Override
	public void destroy() {
		client.getGuiNode().detachAllChildren();
	}


}
