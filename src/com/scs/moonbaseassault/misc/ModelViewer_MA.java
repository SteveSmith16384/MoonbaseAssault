package com.scs.moonbaseassault.misc;

import com.jme3.scene.Spatial;
import com.scs.moonbaseassault.client.SoldierTexture;
import com.scs.stevetech1.jme.JMEModelFunctions;
import com.scs.stevetech1.misc.ModelViewer;

public class ModelViewer_MA extends ModelViewer {

	public static void main(String[] args) {
		ModelViewer_MA app = new ModelViewer_MA();
		app.showSettings = false;

		app.start();
	}


	@Override
	public Spatial getModel() {
		Spatial model = assetManager.loadModel("Models/third_person.blend");
		//Spatial model = assetManager.loadModel("Models/Policeman.fbx");
		//JMEModelFunctions.setTextureOnSpatial(assetManager, model, SoldierTexture.getTexture(false, false));

		JMEModelFunctions.scaleModelToHeight(model, .7f);
		JMEModelFunctions.moveYOriginTo(model, 0f);

		return model;
	}
	
	
	@Override
	public String getAnimNode() {
		return "Human_Mesh (Node)";
	}
	

	@Override
	public String getAnimToShow() {
		return "Walk";
	}
	


}
