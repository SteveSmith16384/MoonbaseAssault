package com.scs.moonbaseassault.client.modules;

import com.scs.stevetech1.client.AbstractGameClient;

public class MainModule extends AbstractModule {
	
	private String ipAddress;
	private int port;

	public MainModule(AbstractGameClient client, String _ipAddress, int _port) {
		super(client);
		
		ipAddress = _ipAddress;
		port = _port;
		
	}
	
	
	@Override
	public void simpleInit() {
		this.client.connect(ipAddress, port);
		this.client.setupForGame();
	}
	

	@Override
	public void simpleUpdate() {
		
	}

}
