package de.mb.heldensoftware.customentries;

import helden.plugin.HeldenDatenPlugin;
import helden.plugin.HeldenPluginFactory;
import org.objectweb.asm.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * This class loads Helden-Software plugins without packing them to jar files lying in the plugin directory.
 * Call {@link #addPlugin(Class)} (possibly multiple times) followed by a call to {@link #registerSideloader()} -
 * before any reference to {@link HeldenPluginFactory} appears.
 */
public class PluginSideloader {

	private static Set<Class<? extends HeldenDatenPlugin>> sideloadedPlugins = new HashSet<>();

	public static void addPlugin(Class<? extends HeldenDatenPlugin> plugin){
		sideloadedPlugins.add(plugin);
	}

	public static void registerSideloader(){
		try {
			patchHeldenPluginFactory();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}




	private static void patchHeldenPluginFactory() throws NoSuchMethodException, IOException, InvocationTargetException, IllegalAccessException {
		ClassLoader classloader = ClassLoader.getSystemClassLoader();
		Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
		defineClass.setAccessible(true);

		// Rewrite HeldenPluginFactory class and load the result using system classloader
		// Tutorial: https://www.javacodegeeks.com/2012/02/manipulating-java-class-files-with-asm.html
		try (InputStream in = classloader.getResourceAsStream("helden/plugin/HeldenPluginFactory.class")){
			ClassReader cr = new ClassReader(in);
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			HeldenPluginFactoryModifierClassWriter mcw = new HeldenPluginFactoryModifierClassWriter(cw);
			cr.accept(mcw, 0);
			byte[] resultingClass = cw.toByteArray();
			defineClass.invoke(classloader, "helden.plugin.HeldenPluginFactory", resultingClass, 0, resultingClass.length);
		}
	}

	public static class HeldenPluginFactoryModifierClassWriter extends ClassAdapter {

		public HeldenPluginFactoryModifierClassWriter(ClassWriter cv) {
			super(cv);
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
			if (name.equals("getAlleHeldenDatenPlugins")) return new MethodModifier(mv);
			return mv;
		}

		// Modifier for getAlleHeldenDatenPlugins
		public static class MethodModifier extends MethodAdapter {

			public MethodModifier(MethodVisitor mv) {
				super(mv);
			}

			@Override
			public void visitInsn(int opcode) {
				// Before "return <list>", add call to hook
				if (opcode == Opcodes.ARETURN){
					String sideloader = PluginSideloader.class.getName().replace('.', '/');
					super.visitMethodInsn(Opcodes.INVOKESTATIC, sideloader, "afterGetAlleHeldenDatenPlugins", "(Ljava/util/List;)Ljava/util/List;");
				}
				super.visitInsn(opcode);
			}
		}
	}

	/**
	 * Patch for {@link HeldenPluginFactory#getAlleHeldenDatenPlugins()}
	 * Adds sideloaded plugins to list
	 * @param plugins
	 * @return
	 */
	public static List<HeldenDatenPlugin> afterGetAlleHeldenDatenPlugins(List<HeldenDatenPlugin> plugins){
		Set<Class<? extends HeldenDatenPlugin>> remainingPlugins = new HashSet<>(sideloadedPlugins);
		for (HeldenDatenPlugin p: plugins){
			remainingPlugins.remove(p.getClass());
		}
		for (Class<? extends HeldenDatenPlugin> plugin: remainingPlugins){
			try {
				plugins.add(plugin.newInstance());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return plugins;
	}

}
