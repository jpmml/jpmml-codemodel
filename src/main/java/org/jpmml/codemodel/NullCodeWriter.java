/*
 * Copyright (c) 2021 Villu Ruusmann
 */
package org.jpmml.codemodel;

import java.io.OutputStream;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JPackage;

public class NullCodeWriter extends CodeWriter {

	@Override
	public OutputStream openBinary(JPackage _package, String name){
		return OutputStream.nullOutputStream();
	}

	@Override
	public void close(){
	}
}