/*
 * Copyright (c) 2018 Villu Ruusmann
 */
package org.jpmml.codemodel;

import com.sun.codemodel.JPackage;

public class FileObjectUtil {

	private FileObjectUtil(){
	}

	static
	public String toResourceName(JPackage _package, String name){

		if(_package.isUnnamed()){
			return name;
		}

		return (_package.name()).replace('.', '/') + "/" + name;
	}
}