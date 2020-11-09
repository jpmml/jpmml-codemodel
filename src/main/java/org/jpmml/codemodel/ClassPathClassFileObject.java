/*
 * Copyright (c) 2018 Villu Ruusmann
 */
package org.jpmml.codemodel;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import com.google.common.reflect.ClassPath;

public class ClassPathClassFileObject extends ClassFileObject {

	private ClassPath.ClassInfo classInfo = null;


	public ClassPathClassFileObject(ClassPath.ClassInfo classInfo){
		super("classpath", classInfo != null ? classInfo.getName() : null);

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
		this.classInfo = Objects.requireNonNull(classInfo);
	}
}