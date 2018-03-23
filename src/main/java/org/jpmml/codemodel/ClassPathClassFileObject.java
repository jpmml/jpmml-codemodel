/*
 * Copyright (c) 2018 Villu Ruusmann
 */
package org.jpmml.codemodel;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.tools.SimpleJavaFileObject;

import com.google.common.reflect.ClassPath;

public class ClassPathClassFileObject extends SimpleJavaFileObject {

	private ClassPath.ClassInfo classInfo = null;


	public ClassPathClassFileObject(String name, ClassPath.ClassInfo classInfo){
		super(URI.create("classpath:///" + name), Kind.CLASS);

		setClassInfo(classInfo);
	}

	@Override
	public InputStream openInputStream() throws IOException {
		ClassPath.ClassInfo classInfo = getClassInfo();

		return (classInfo.url()).openStream();
	}

	public String getBinaryName(){
		ClassPath.ClassInfo classInfo = getClassInfo();

		return classInfo.getName();
	}

	public ClassPath.ClassInfo getClassInfo(){
		return this.classInfo;
	}

	private void setClassInfo(ClassPath.ClassInfo classInfo){
		this.classInfo = classInfo;
	}
}