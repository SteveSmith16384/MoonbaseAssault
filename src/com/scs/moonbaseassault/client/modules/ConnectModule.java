package com.scs.moonbaseassault.client.modules;

import com.scs.moonbaseassault.client.MoonbaseAssaultClient;

public class ConnectModule extends AbstractModule {
	
	private String ipAddress;
	private int port;

	public ConnectModule(MoonbaseAssaultClient client, String _ipAddress, int _port) {
		super(client);

		ipAddress = _ipAddress;
		port = _port;
		
	}

	
	@Override
	public void simpleInit() {
		this.client.connect(client, ipAddress, port, true);
		
	}
	

	@Override
	public void simpleUpdate(float tpfSecs) {
		if (this.client.lastConnectException != null) {
			// todo
		}
	}
	

	@Override
	public void destroy() {
		
	}

}
