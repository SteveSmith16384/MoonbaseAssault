package com.scs.moonbaseassault.client.modules;

public interface IModule {

	void simpleInit();
	
	void simpleUpdate(float tpfSecs);
	
	void destroy();
}
