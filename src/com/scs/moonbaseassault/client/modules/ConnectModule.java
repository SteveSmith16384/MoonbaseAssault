package com.scs.moonbaseassault.client.modules;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.scs.moonbaseassault.client.MoonbaseAssaultClient;

public class ConnectModule extends AbstractModule {
	
	private String ipAddress;
	private int port;

	private BitmapText bmpText;
	
	public ConnectModule(MoonbaseAssaultClient client, String _ipAddress, int _port) {
		super(client);

		ipAddress = _ipAddress;
		port = _port;

		BitmapFont font_small = client.getAssetManager().loadFont("Interface/Fonts/Console.fnt");

		BitmapText bmpText = new BitmapText(font_small, false);
		bmpText.setColor(IntroModule.defaultColour);
		bmpText.setLocalTranslation(100, 100, 0);
		client.getGuiNode().attachChild(bmpText);
		bmpText.setText("Connecting...");
		

	}

	
	@Override
	public void simpleInit() {
		this.client.connect(client, ipAddress, port, true);
		
	}
	

	@Override
	public void simpleUpdate(float tpfSecs) {
		if (this.client.lastConnectException != null) {
			bmpText.setText("Failed to connect to server (" + this.client.lastConnectException.getMessage() + ")");
		}
	}
	

	@Override
	public void destroy() {
		client.getGuiNode().detachAllChildren();
	}

}
