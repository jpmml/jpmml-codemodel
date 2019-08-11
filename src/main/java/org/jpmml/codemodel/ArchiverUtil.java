/*
 * Copyright (c) 2018 Villu Ruusmann
 */
package org.jpmml.codemodel;

import java.io.IOException;
import java.io.OutputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JCodeModel;

public class ArchiverUtil {

	private ArchiverUtil(){
	}

	static
	public void archive(JCodeModel codeModel, OutputStream os) throws IOException {
		Manifest manifest = new Manifest();

		Attributes attributes = manifest.getMainAttributes();
		attributes.putValue("Manifest-Version", "1.0");

		CodeWriter zipWriter = new JarCodeWriter(os, manifest);

		codeModel.build(zipWriter);
	}
}