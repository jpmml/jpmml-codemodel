/*
 * Copyright (c) 2018 Villu Ruusmann
 */
package org.jpmml.codemodel;

import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class ArchiverUtil {

	private ArchiverUtil(){
	}

	static
	public Manifest createManifest(){
		return createManifest(null);
	}

	static
	public Manifest createManifest(Class<?> creator){
		Manifest manifest = new Manifest();

		Attributes mainAttributes = manifest.getMainAttributes();
		mainAttributes.putValue("Manifest-Version", "1.0");

		if(creator != null){
			Package _package = creator.getPackage();

			mainAttributes.putValue("Created-By", _package.getImplementationTitle() + " " + _package.getImplementationVersion());
		}

		return manifest;
	}
}