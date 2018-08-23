package com.scs.moonbaseassault.client.modules;

public interface IModule {

	void simpleInit();
	
	boolean onAction(String name, boolean value, float tpf); // todo - change to mouseClicked

	void simpleUpdate(float tpfSecs);
	
	void destroy();
}
