package com.scs.moonbaseassault.client.modules;

public interface IModule {

	void simpleInit();
	
	void mouseClicked();

	void simpleUpdate(float tpfSecs);
	
	void destroy();
}
