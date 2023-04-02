package de.mb.heldensoftware.customentries;

import org.objectweb.asm.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Created by Markus on 29.03.2017.
 */
public class InstrumentationEngine {

	private final String classname;
	private final HashMap<String, MethodModifierGenerator> methodInstrumentation = new HashMap<>();

	public InstrumentationEngine(String classname) {
		this.classname = classname;
	}

	public void addMethodInstrumentation(String name, MethodModifierGenerator gen){
		// only one inst per method by now
		methodInstrumentation.put(name, gen);
	}

	public void patchClass() throws NoSuchMethodException, IOException, InvocationTargetException, IllegalAccessException {
		ClassLoader classloader = ClassLoader.getSystemClassLoader();
		Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
		defineClass.setAccessible(true);

		// Rewrite class and load the result using system classloader
		// Tutorial: https://www.javacodegeeks.com/2012/02/manipulating-java-class-files-with-asm.html
		String classfile = classname.replace('.', '/') + ".class";
		try (InputStream in = classloader.getResourceAsStream(classfile)){
			ClassReader cr = new ClassReader(in);
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			ModifierClassWriter mcw = new ModifierClassWriter(cw);
			cr.accept(mcw, 0);
			byte[] resultingClass = cw.toByteArray();
			defineClass.invoke(classloader, classname, resultingClass, 0, resultingClass.length);
		}
	}


	private class ModifierClassWriter extends ClassAdapter {

		public ModifierClassWriter(ClassWriter cv) {
			super(cv);
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
			MethodModifierGenerator gen = methodInstrumentation.get(name);
			if (gen != null)
				return gen.getMethodVisitor(mv, access, name, desc, signature, exceptions);
			return mv;
		}
	}



	public static interface MethodModifierGenerator {
		public MethodVisitor getMethodVisitor(MethodVisitor parent, int access, String name, String desc, String signature, String[] exceptions);
	}



	/**
	 * Wraps every "return X" by a static method call: "return <classname>.<method>(X)". X must be an object type (or boolean).
	 */
	public static class MethodResultAModifier implements MethodModifierGenerator {
		private final String wrapperResultType; // can be null
		private final String classname;
		private final String method;
		private final String thisReferenceType;

		public MethodResultAModifier(String classname, String method, String resultType) {
			this.wrapperResultType = resultType;
			this.classname = classname;
			this.method = method;
			this.thisReferenceType = null;
		}

		public MethodResultAModifier(String classname, String method) {
			this(classname, method, null);
		}

		public MethodResultAModifier(Class c, String method, String resultType) {
			this.classname = c.getName().replace('.', '/');
			this.method = method;
			this.wrapperResultType = resultType;
			this.thisReferenceType = null;
		}

		public MethodResultAModifier(Class c, String method) {
			this(c, method, null);
		}

		public MethodResultAModifier(Method m){
			this.classname = m.getDeclaringClass().getName().replace('.', '/');
			this.method = m.getName();
			this.wrapperResultType = "L"+m.getReturnType().getName().replace('.', '/');
			Class[] params = m.getParameterTypes();
			if (params.length == 2){
				this.thisReferenceType = "L"+params[1].getName().replace('.', '/');
			}else{
				this.thisReferenceType = null;
			}
		}

		private String extractResultType(String desc){
			int p = desc.lastIndexOf(')');
			desc = desc.substring(p+1);
			if (desc.endsWith(";"))
				desc = desc.substring(0, desc.length() - 1);
			return desc;
		}

		@Override
		public MethodVisitor getMethodVisitor(MethodVisitor parent, int access, String name, String desc, String signature, String[] exceptions) {
			final String originalResultType = extractResultType(desc);
			final String methodResultType = wrapperResultType != null ? wrapperResultType : originalResultType;
			final boolean isIntReturn = methodResultType.equals("Z");
			final String callbackType = isIntReturn
					? "(" + methodResultType + ")" + methodResultType
					: "(" + methodResultType + ";" + (thisReferenceType == null ? "" : thisReferenceType + ";") + ")" + methodResultType + ";";

			return new MethodAdapter(parent) {
				@Override
				public void visitInsn(int opcode) {
					// Before "return X", add call to hook
					if (!isIntReturn && opcode == Opcodes.ARETURN) {
						if (thisReferenceType != null)
							super.visitIntInsn(Opcodes.ALOAD, 0);
						if (!originalResultType.equals(methodResultType)) {
							super.visitTypeInsn(Opcodes.CHECKCAST, methodResultType.substring(1));
						}
						super.visitMethodInsn(Opcodes.INVOKESTATIC, classname, method, callbackType);
						// if return types do not match, we add a cast (just to be sure)
						if (!originalResultType.equals(methodResultType)) {
							super.visitTypeInsn(Opcodes.CHECKCAST, originalResultType.substring(1));
						}
					} else if (isIntReturn && opcode == Opcodes.IRETURN) {
						if (thisReferenceType != null)
							super.visitIntInsn(Opcodes.ILOAD, 0);
						super.visitMethodInsn(Opcodes.INVOKESTATIC, classname, method, callbackType);
					}
					super.visitInsn(opcode);
				}
			};
		}
	}
}
