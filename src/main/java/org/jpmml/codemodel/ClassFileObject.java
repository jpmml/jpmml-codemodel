/*
 * Copyright (c) 2019 Villu Ruusmann
 */
package org.jpmml.codemodel;

import java.net.URI;

import javax.tools.SimpleJavaFileObject;

abstract
public class ClassFileObject extends SimpleJavaFileObject {

	public ClassFileObject(String scheme, String name){
		super(URI.create(scheme + ":///" + name.replace('.', '/') + ".class"), Kind.CLASS);
	}

	/**
	 * @see Class#getSimpleName()
	 */
	public String getSimpleName(){
		String name = getName();

		String simpleName;

		int slash = name.lastIndexOf('/');
		if(slash > -1){
			simpleName = name.substring(slash + 1);
		} else

		{
			simpleName = name;
		} // End if

		if(simpleName.endsWith(".class")){
			simpleName = simpleName.substring(0, simpleName.length() - ".class".length());
		} else

		{
			throw new IllegalStateException(name);
		}

		return simpleName;
	}

	/**
	 * @see Class#getPackage()
	 * @see Package#getName()
	 */
	public String getPackageName(){
		String name = getName();

		String packageName;

		int slash = name.lastIndexOf('/');
		if(slash > -1){
			packageName = name.substring(0, slash);

			if(packageName.startsWith("/")){
				packageName = packageName.substring("/".length());
			}
		} else

		{
			packageName = "";
		}

		return packageName.replace('/', '.');
	}
}