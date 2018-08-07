package com.scs.moonbaseassault.client.modules;

import com.scs.stevetech1.client.AbstractGameClient;

public abstract class AbstractModule implements IModule {

	protected AbstractGameClient client;
	
	public AbstractModule(AbstractGameClient _client) {
		super();
		
		client = _client;
	}

}
