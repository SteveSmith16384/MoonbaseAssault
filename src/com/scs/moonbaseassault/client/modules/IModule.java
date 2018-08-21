package com.scs.moonbaseassault.client.modules;

import java.io.IOException;

public interface IModule {

	void simpleInit() throws IOException;
	
	boolean onAction(String name, boolean value, float tpf); // todo - change to mouseClicked

	void simpleUpdate(float tpfSecs);
	
	void destroy();
}
