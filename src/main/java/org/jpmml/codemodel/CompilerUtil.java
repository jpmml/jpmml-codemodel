/*
 * Copyright (c) 2018 Villu Ruusmann
 */
package org.jpmml.codemodel;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.reflect.ClassPath;
import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JPackage;

public class CompilerUtil {

	private CompilerUtil(){
	}

	static
	public void compile(JCodeModel codeModel) throws IOException {
		ClassLoader classLoader = (Thread.currentThread()).getContextClassLoader();

		compile(codeModel, classLoader);
	}

	static
	public void compile(JCodeModel codeModel, ClassLoader classLoader) throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

		if(compiler == null){
			throw new IOException();
		}

		List<StringSourceFileObject> sourceObjects = new ArrayList<>();
		List<ByteArrayClassFileObject> classObjects = new ArrayList<>();

		CodeWriter sourceWriter = createSourceObjectCodeWriter(sourceObjects);

		codeModel.build(sourceWriter);

		boolean success;

		try(StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(null, null, null)){

			try(JavaFileManager classObjectFileManager = createClassObjectJavaFileManager(standardFileManager, classLoader, classObjects)){
				JavaCompiler.CompilationTask task = compiler.getTask(null, classObjectFileManager, null, null, null, sourceObjects);

				success = task.call();
			}
		}

		if(!success){
			throw new IOException();
		}

		for(ByteArrayClassFileObject classObject : classObjects){
			String name = classObject.getName();

			// Convert URI absolute path to relative path
			if(name.startsWith("/")){
				name = name.substring(1);
			}

			JPackage _package;

			int slash = name.lastIndexOf('/');
			if(slash > -1){
				_package = codeModel._package((name.substring(0, slash)).replace('/', '.'));

				name = name.substring(slash + 1);
			} else

			{
				_package = codeModel.rootPackage();
			}

			JClassFile classFile = new JClassFile(name);
			classFile.setBytes(classObject.toByteArray());

			_package.addResourceFile(classFile);
		}
	}

	static
	private CodeWriter createSourceObjectCodeWriter(final List<StringSourceFileObject> sourceObjects){
		CodeWriter codeWriter = new CodeWriter(){

			private StringSourceFileObject sourceObject = null;


			@Override
			public OutputStream openBinary(JPackage _package, String name){
				openSourceObject(FileObjectUtil.toResourceName(_package, name));

				FilterOutputStream result = new FilterOutputStream(this.sourceObject.openOutputStream()){

					@Override
					public void close() throws IOException {
						super.close();

						closeSourceObject();
					}
				};

				return result;
			}

			@Override
			public void close(){
			}

			private void openSourceObject(String name){
				this.sourceObject = new StringSourceFileObject(name, super.encoding);
			}

			private void closeSourceObject(){
				sourceObjects.add(this.sourceObject);

				this.sourceObject = null;
			}
		};

		return codeWriter;
	}

	static
	private JavaFileManager createClassObjectJavaFileManager(StandardJavaFileManager standardFileManager, final ClassLoader classLoader, final List<ByteArrayClassFileObject> classObjects){
		JavaFileManager fileManager = new ForwardingJavaFileManager<StandardJavaFileManager>(standardFileManager){

			private ClassPath classPath = null;


			@Override
			public Iterable<JavaFileObject> list(JavaFileManager.Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException {
				Iterable<JavaFileObject> result = super.list(location, packageName, kinds, recurse);

				if((StandardLocation.CLASS_PATH).equals(location) && kinds.contains(JavaFileObject.Kind.CLASS)){
					Collection<ClassPathClassFileObject> classLoaderObjects = listClassObjects(packageName, recurse);

					if(classLoaderObjects.size() > 0){
						result = Iterables.concat(result, classLoaderObjects);
					}
				}

				return result;
			}

			@Override
			public String inferBinaryName(JavaFileManager.Location location, JavaFileObject fileObject){

				if((StandardLocation.CLASS_PATH).equals(location) && (fileObject instanceof ClassPathClassFileObject)){
					ClassPathClassFileObject classObject = (ClassPathClassFileObject)fileObject;

					return classObject.getBinaryName();
				}

				return super.inferBinaryName(location, fileObject);
			}

			@Override
			public JavaFileObject getJavaFileForOutput(JavaFileManager.Location location, String name, JavaFileObject.Kind kind, FileObject sibling) throws IOException {

				if((JavaFileObject.Kind.CLASS).equals(kind)){
					ByteArrayClassFileObject classObject = new ByteArrayClassFileObject(name.replace('.', '/') + ".class");

					classObjects.add(classObject);

					return classObject;
				}

				return super.getJavaFileForOutput(location, name, kind, sibling);
			}

			private Collection<ClassPathClassFileObject> listClassObjects(final String packageName, final boolean recurse) throws IOException {
				List<ClassPathClassFileObject> result = new ArrayList<>();

				Predicate<ClassPath.ClassInfo> filter = new Predicate<ClassPath.ClassInfo>(){

					@Override
					public boolean apply(ClassPath.ClassInfo classInfo){

						if(recurse){
							return (classInfo.getName()).startsWith(packageName + ".");
						}

						return (classInfo.getPackageName()).equals(packageName);
					}
				};

				ClassPath classPath = getClassPath();

				Iterable<ClassPath.ClassInfo> classInfos = Iterables.filter(classPath.getAllClasses(), filter);
				for(ClassPath.ClassInfo classInfo : classInfos){
					ClassPathClassFileObject classObject = new ClassPathClassFileObject((classInfo.getName()).replace('.', '/') + ".class", classInfo);

					result.add(classObject);
				}

				return result;
			}

			private ClassPath getClassPath() throws IOException {

				if(this.classPath == null){
					this.classPath = ClassPath.from(classLoader);
				}

				return this.classPath;
			}
		};

		return fileManager;
	}
}