/*
 * Copyright (c) 2018 Villu Ruusmann
 */
package org.jpmml.codemodel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CodeModelTest {

	@Test
	public void compileAndPackage() throws Exception {
		JCodeModel codeModel = new JCodeModel();

		generateExampleApplication(codeModel, "example.Main");

		CompilerUtil.compile(codeModel);

		File file = File.createTempFile("codemodel", ".jar");

		try(OutputStream os = new FileOutputStream(file)){
			ArchiverUtil.archive(os, codeModel);
		}

		try(JarFile jarFile = new JarFile(file)){
			Manifest manifest = jarFile.getManifest();
			assertNotNull(manifest);

			JarEntry javaEntry = jarFile.getJarEntry("example/Main.java");
			assertNotNull(javaEntry);
			assertTrue(javaEntry.getSize() > 0L);

			JarEntry classEntry = jarFile.getJarEntry("example/Main.class");
			assertNotNull(classEntry);
			assertTrue(classEntry.getSize() > 0L);
		}

		boolean success = file.delete();

		assertTrue(success);
	}

	static
	private void generateExampleApplication(JCodeModel codeModel, String name) throws JClassAlreadyExistsException {
		JDefinedClass mainClazz = codeModel._class(JMod.PUBLIC, name, ClassType.CLASS);

		JMethod mainMethod = mainClazz.method(JMod.PUBLIC | JMod.STATIC, void.class, "main");

		mainMethod.varParam(String.class, "args");

		JBlock mainMethodBody = mainMethod.body();

		mainMethodBody.directStatement("System.out.println(\"Hello World\");");
	}
}