package org.pentaho.di;

import java.io.File;
import java.sql.Driver;
import java.util.Map;
import java.util.Set;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import junit.framework.TestCase;

import org.scannotation.AnnotationDB;

public class JaTest extends TestCase {

	public void testJavaAssist() throws Exception {
		File file = new File("/tmp/JDBC/ojdbc14.jar");
		AnnotationDB db = new AnnotationDB();
		db.scanArchives(file.toURI().toURL());
		Map<String, Set<String>> classIndex = db.getClassIndex();

		ClassPool classPool = ClassPool.getDefault();
		classPool.insertClassPath(file.toString());
		for (String key : classIndex.keySet()) {
			CtClass ctClass = classPool.get(key);
	
			try {
				CtClass[] interfaces = ctClass.getInterfaces();
				for (CtClass interf : interfaces) {
					if (interf.getName().equals(Driver.class.getName())) {
						System.out.println(file.toString()+"     --> "+key); // <<<<----- found!
					}
				}
			} catch(NotFoundException e) {
				// System.out.println("        - interfaces not found for class: "+ctClass.getName());
			}
		}		
	}
}
