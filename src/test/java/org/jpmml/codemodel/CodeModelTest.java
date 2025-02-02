/*
 * Copyright (c) 2018 Villu Ruusmann
 */
package org.jpmml.codemodel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.tools.JavaCompiler;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CodeModelTest {

	@Test
	public void compileAndArchive() throws Exception {
		compileAndArchive(null);
		compileAndArchive(new EclipseCompiler());
	}

	@Test
	public void compileAndLoad() throws Exception {
		compileAndLoad(null);
		compileAndLoad(new EclipseCompiler());
	}

	static
	private void compileAndArchive(JavaCompiler compiler) throws Exception {
		JCodeModel codeModel = new JCodeModel();

		generateExampleApplication(codeModel, "example.Main");

		CompilerUtil.compile(codeModel, compiler, null, null);

		File file = File.createTempFile("codemodel", ".jar");

		try(OutputStream os = new FileOutputStream(file)){
			Manifest manifest = ArchiverUtil.createManifest();

			CodeWriter codeWriter = new JarCodeWriter(os, manifest);

			codeModel.build(codeWriter);
		}

		try(JarFile jarFile = new JarFile(file)){
			Manifest manifest = jarFile.getManifest();
			assertNotNull(manifest);

			Attributes mainAttributes = manifest.getMainAttributes();
			assertNotNull(mainAttributes.getValue("Manifest-Version"));
			assertNull(mainAttributes.getValue("Created-By"));

			JarEntry javaEntry = jarFile.getJarEntry("example/Main.java");
			assertNotNull(javaEntry);
			assertTrue(javaEntry.getSize() > 0L);

			JarEntry classEntry = jarFile.getJarEntry("example/Main.class");
			assertNotNull(classEntry);
			assertTrue(classEntry.getSize() > 0L);
		}

		JCodeModel derivedCodeModel = new JCodeModel();

		generateDerivedExampleApplication(derivedCodeModel, "Main", "example.Main");

		URL[] classpath = {
			((file.toURI()).toURL())
		};

		try(URLClassLoader classLoader = new URLClassLoader(classpath)){
			CompilerUtil.compile(derivedCodeModel, compiler, null, classLoader);
		}

		boolean success = file.delete();

		assertTrue(success);
	}

	static
	private void compileAndLoad(JavaCompiler compiler) throws Exception {
		JCodeModel codeModel = new JCodeModel();

		generateExampleApplication(codeModel, "example.Main");

		CompilerUtil.compile(codeModel, compiler, null, null);

		ClassLoader classLoader = new JCodeModelClassLoader(codeModel);

		Class<?> clazz = classLoader.loadClass("example.Main");
		assertEquals("example.Main", clazz.getName());

		URL classResource = classLoader.getResource("example/Main.class");
		assertNotNull(classResource);
	}

	static
	private void generateExampleApplication(JCodeModel codeModel, String name) throws JClassAlreadyExistsException {
		JDefinedClass mainClazz = codeModel._class(JMod.PUBLIC, name, ClassType.CLASS);

		JMethod mainMethod = mainClazz.method(JMod.PUBLIC | JMod.STATIC, void.class, "main");

		mainMethod.varParam(String.class, "args");

		JBlock mainMethodBody = mainMethod.body();

		mainMethodBody.directStatement("System.out.println(\"Hello World\");");
	}

	static
	private void generateDerivedExampleApplication(JCodeModel codeModel, String name, String mainName) throws JClassAlreadyExistsException {
		JDefinedClass derivedClazz = codeModel._class(JMod.PUBLIC, name, ClassType.CLASS);
		derivedClazz._extends(codeModel.ref(mainName));
	}
}