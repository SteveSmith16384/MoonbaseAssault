package com.scs.moonbaseassault.client.modules;

import java.io.IOException;

public interface IModule {

	void simpleInit() throws IOException;
	
	void simpleUpdate(float tpfSecs);
	
	void destroy();
}
