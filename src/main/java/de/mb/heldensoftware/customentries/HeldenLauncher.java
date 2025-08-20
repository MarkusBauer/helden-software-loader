package de.mb.heldensoftware.customentries;

import de.mb.heldensoftware.customentries.EntryCreator.*;
import helden.Helden;

import javax.print.URIException;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Markus on 19.03.2017.
 */
public class HeldenLauncher {

	public static void main(String[] args) {
        HeldenLauncher launcher = new HeldenLauncher();
        launcher.registerBundledPlugins();
        launcher.sideloaderWithPotentialRestart(args);
        launcher.prepareEntryLoader();
        // Launch Helden-Software
		Helden.main(args);
	}

    protected void sideloaderWithPotentialRestart(String[] args) {
        try {
			// Register the plugin component
			PluginSideloader.registerSideloader();
		} catch (RuntimeException e) {
			// Java 9+ module API prevents us from modifying the classloader.
			if (e.getClass().getName().contains("InaccessibleObjectException")) {
				restart(args);
				JOptionPane.showMessageDialog(null,
						"Die Java-Konfiguration verbietet Modifikationen am Class-Loader. \n\n" +
						"Um Modifikationen zu erlauben, sind diese Start-Parameter notwendig (zwischen java und -jar):\n"+
						"--add-opens java.base/java.lang=ALL-UNNAMED\n\n"+
						"Alternativ können Sie eine ältere Java-Version einsetzen (bspw. Java 11).",
						"Java Configuration Error", JOptionPane.ERROR_MESSAGE);
			}
			throw e;
		}
    }

    protected void prepareEntryLoader() {
        // Patch bugs
        ErrorHandler.patchHeldenErrorHandler();
        ModsDatenParserBugPatcher.patchModsDatenParser();
        // Resolve reflection references (after patches are deployed, before HeldenSoftware initializes anything)
        EntryCreator.getInstance();
        // Load the non-plugin component
        CustomEntryLoader.loadFiles();
    }

    protected void registerBundledPlugins() {
        PluginSideloader.addPlugin(CustomEntryLoaderPlugin.class);
    }

    public void restart(String[] args) {
		if (System.getenv().containsKey("CUSTOMENTRYLOADER_INVOKED"))
			return;
		try {
			ArrayList<String> command = new ArrayList<>();

			// TODO JVM BINARY
			// String jvmBinary = System.console() == null ? "javaw" : "java";
			String jvmBinary = new File(new File(System.getProperty("java.home"), "bin"), "java").getPath();
			if (System.console() == null && (new File(jvmBinary + "w").exists() || new File(jvmBinary + "w.exe").exists())) {
				jvmBinary += "w";
			}
			if (!new File(jvmBinary).exists() && new File(jvmBinary + ".exe").exists()) {
				jvmBinary += ".exe";
			}
			if (!new File(jvmBinary).exists()) {
				System.err.println("JVM binary " + jvmBinary + " does not exist.");
				return;
			}
			command.add(jvmBinary);

			// JVM ARGS
            additionalJvmArgs(command);

            // The JAR
			String jarPath = Paths.get(getClass()
					.getProtectionDomain()
					.getCodeSource()
					.getLocation()
					.toURI())
					.toFile().getAbsolutePath();
			if (jarPath.endsWith(".jar")) {
				command.add("-jar");
				command.add(jarPath);
			} else {
				command.add(HeldenLauncher.class.getName());
			}

			// Args
			command.addAll(Arrays.asList(args));

			// Debug command
			System.out.println("Restarting reconfigured JVM with classloader module exceptions.");
			System.out.print("Delegate to:");
			for (String s : command) {
				System.out.print(" " + s);
			}
			System.out.println("");

			try {
				long time = System.currentTimeMillis();
				ProcessBuilder pb = new ProcessBuilder().inheritIO().command(command);
				pb.environment().put("CUSTOMENTRYLOADER_INVOKED", "1");
				Process p = pb.start();
				int code = p.waitFor();
				if (code != 0 && System.currentTimeMillis() - time < 2000) {
					// Executing errored
					System.err.println("Execution failed with code " + code);
					return;
				}

				// Execution was ok, nothing to do anymore
				System.exit(0);
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

    protected void additionalJvmArgs(ArrayList<String> command) {
        command.addAll(ManagementFactory.getRuntimeMXBean().getInputArguments());
        command.add("-classpath");
        command.add(System.getProperty("java.class.path"));
        command.add("--add-opens");
        command.add("java.base/java.lang=ALL-UNNAMED");
        // we might need this argument for other plugins
        command.add("-Djava.security.manager=allow");
    }

}
