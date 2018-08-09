package com.scs.moonbaseassault.client.modules;

import com.scs.moonbaseassault.client.MoonbaseAssaultClient;

public abstract class AbstractModule implements IModule {

	protected MoonbaseAssaultClient client;
	
	public AbstractModule(MoonbaseAssaultClient _client) {
		super();
		
		client = _client;
	}

}
