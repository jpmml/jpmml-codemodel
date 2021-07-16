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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.SetMultimap;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.Reflection;
import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JPackage;

public class CompilerUtil {

	private CompilerUtil(){
	}

	static
	public void compile(JCodeModel codeModel) throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if(compiler == null){
			throw new IOException();
		}

		compile(codeModel, compiler);
	}

	static
	public void compile(JCodeModel codeModel, JavaCompiler compiler) throws IOException {
		ClassLoader classLoader = (Thread.currentThread()).getContextClassLoader();

		compile(codeModel, compiler, classLoader);
	}

	static
	public void compile(JCodeModel codeModel, JavaCompiler compiler, ClassLoader classLoader) throws IOException {
		List<StringSourceFileObject> sourceObjects = new ArrayList<>();
		List<ByteArrayClassFileObject> classObjects = new ArrayList<>();

		CodeWriter sourceWriter = createSourceObjectCodeWriter(sourceObjects);
		CodeWriter resourceWriter = createResourceCodeWriter();

		codeModel.build(sourceWriter, resourceWriter);

		DiagnosticCollector<? super JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();

		boolean success;

		try(StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(diagnosticCollector, null, null)){

			try(JavaFileManager classObjectFileManager = createClassObjectJavaFileManager(standardFileManager, classLoader, classObjects)){
				JavaCompiler.CompilationTask task = compiler.getTask(null, classObjectFileManager, diagnosticCollector, null, null, sourceObjects);

				List<Diagnostic<?>> diagnostics = (List)diagnosticCollector.getDiagnostics();
				for(Diagnostic<?> diagnostic : diagnostics){
					System.err.println(diagnostic);
				}

				success = task.call();
			}
		}

		if(!success){
			throw new IOException();
		}

		for(ByteArrayClassFileObject classObject : classObjects){
			JClassFile classFile = classObject.toClassFile();

			JPackage _package;

			String packageName = classObject.getPackageName();
			if(("").equals(packageName)){
				_package = codeModel.rootPackage();
			} else

			{
				_package = codeModel._package(packageName);
			}

			_package.addResourceFile(classFile);
		}
	}

	static
	private CodeWriter createSourceObjectCodeWriter(List<StringSourceFileObject> sourceObjects){
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
	private CodeWriter createResourceCodeWriter(){
		CodeWriter codeWriter = new NullCodeWriter();

		return codeWriter;
	}

	static
	private JavaFileManager createClassObjectJavaFileManager(StandardJavaFileManager standardFileManager, ClassLoader classLoader, List<ByteArrayClassFileObject> classObjects){
		JavaFileManager fileManager = new ForwardingJavaFileManager<StandardJavaFileManager>(standardFileManager){

			private SetMultimap<String, ClassPath.ClassInfo> classPathMap = null;


			@Override
			public Iterable<JavaFileObject> list(JavaFileManager.Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException {
				Iterable<JavaFileObject> result = super.list(location, packageName, kinds, recurse);

				if((StandardLocation.CLASS_PATH).equals(location) && kinds.contains(JavaFileObject.Kind.CLASS)){
					Collection<ClassPathClassFileObject> classLoaderObjects = listClassFileObjects(packageName.replace('/', '.'), recurse);

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
			public JavaFileObject getJavaFileForInput(JavaFileManager.Location location, String name, Kind kind) throws IOException {

				if((StandardLocation.CLASS_PATH).equals(location) && (JavaFileObject.Kind.CLASS).equals(kind)){
					ClassPathClassFileObject classLoaderObject = getClassFileObject(name.replace('/', '.'));

					if(classLoaderObject != null){
						return classLoaderObject;
					}
				}

				return super.getJavaFileForInput(location, name, kind);
			}

			@Override
			public JavaFileObject getJavaFileForOutput(JavaFileManager.Location location, String name, JavaFileObject.Kind kind, FileObject sibling) throws IOException {

				if((StandardLocation.CLASS_OUTPUT).equals(location) && (JavaFileObject.Kind.CLASS).equals(kind)){
					ByteArrayClassFileObject classObject = new ByteArrayClassFileObject(name);

					classObjects.add(classObject);

					return classObject;
				}

				return super.getJavaFileForOutput(location, name, kind, sibling);
			}

			private Collection<ClassPathClassFileObject> listClassFileObjects(String packageName, boolean recurse) throws IOException {
				SetMultimap<String, ClassPath.ClassInfo> classPathMap = getClassPathMap();

				Collection<? extends Map.Entry<String, Collection<ClassPath.ClassInfo>>> entries = (classPathMap.asMap()).entrySet();

				Set<ClassPathClassFileObject> result = entries.stream()
					.filter(entry -> recurse ? (entry.getKey()).startsWith(packageName + ".") : (entry.getKey()).equals(packageName))
					.flatMap(entry -> (entry.getValue()).stream())
					.map(classInfo -> new ClassPathClassFileObject(classInfo))
					.collect(Collectors.toSet());

				return result;
			}

			private ClassPathClassFileObject getClassFileObject(String name) throws IOException {
				SetMultimap<String, ClassPath.ClassInfo> classPathMap = getClassPathMap();

				Set<ClassPath.ClassInfo> classInfos = classPathMap.get(Reflection.getPackageName(name));
				if(classInfos != null){
					ClassPathClassFileObject result = classInfos.stream()
						.filter(classInfo -> (classInfo.getName()).equals(name))
						.map(classInfo -> new ClassPathClassFileObject(classInfo))
						.findFirst().orElse(null);

					return result;
				}

				return null;
			}

			private SetMultimap<String, ClassPath.ClassInfo> getClassPathMap() throws IOException {

				if(this.classPathMap == null){
					this.classPathMap = createClassPathMap();
				}

				return this.classPathMap;
			}

			private SetMultimap<String, ClassPath.ClassInfo> createClassPathMap() throws IOException {
				ClassPath classPath = ClassPath.from(classLoader);

				ImmutableSetMultimap.Builder<String, ClassPath.ClassInfo> resultBuilder = ImmutableSetMultimap.builder();

				ImmutableSet<ClassPath.ClassInfo> classInfos = classPath.getAllClasses();
				for(ClassPath.ClassInfo classInfo : classInfos){
					resultBuilder.put(Reflection.getPackageName(classInfo.getName()), classInfo);
				}

				return resultBuilder.build();
			}
		};

		return fileManager;
	}
}