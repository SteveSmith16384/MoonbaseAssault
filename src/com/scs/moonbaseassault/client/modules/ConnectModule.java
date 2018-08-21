package com.scs.moonbaseassault.client.modules;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.scs.moonbaseassault.client.MoonbaseAssaultClient;

public class ConnectModule extends AbstractModule { //implements ActionListener {

	private String ipAddress;
	private int port;

	private BitmapText bmpText;

	public ConnectModule(MoonbaseAssaultClient client, String _ipAddress, int _port) {
		super(client);

		ipAddress = _ipAddress;
		port = _port;
	}


	@Override
	public void simpleInit() {
		/*		super.simpleInit();

		client.getInputManager().addMapping("Ability1", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
		client.getInputManager().addListener(this, "Ability1");
		 */
		BitmapFont font_small = client.getAssetManager().loadFont("Interface/Fonts/Console.fnt");

		bmpText = new BitmapText(font_small, false);
		bmpText.setColor(IntroModule.defaultColour);
		bmpText.setLocalTranslation(10, 100, 0);
		client.getGuiNode().attachChild(bmpText);
		bmpText.setText("Connecting...");

		this.client.connect(client, ipAddress, port, true);		
	}


	@Override
	public void simpleUpdate(float tpfSecs) {
		super.simpleUpdate(tpfSecs);

		if (this.client.lastConnectException != null) {
			bmpText.setText("Failed to connect to server (" + this.client.lastConnectException.getMessage() + ")\n\nClick to try again");
		} else if (client.isConnected()) {
			client.showPreGameModule();
		}
	}


	@Override
	public boolean onAction(String name, boolean value, float tpf) {
		if (name.equalsIgnoreCase("Ability1")) {
			if (client.isConnecting() == false) {
				this.client.lastConnectException = null;
				bmpText.setText("Connecting...");
				this.client.connect(client, ipAddress, port, true);
				return true;
			}
		}
		return false;
	}


	@Override
	public void destroy() {
		client.getGuiNode().detachAllChildren();
	}

}
