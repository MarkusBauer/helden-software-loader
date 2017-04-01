package de.mb.heldensoftware.customentries;

import java.lang.reflect.Field;

/**
 * Created by Markus on 01.04.2017.
 */
public class ErrorHandler {

	/**
	 * Handle exceptions that should not occur (or shouldn't be handled otherwise).
	 * Avoids unnecessary RuntimeException-wrapping.
	 * This method never returns.
	 * @param t
	 */
	public static void handleException(Throwable t){
		if (t instanceof RuntimeException) throw (RuntimeException) t;
		throw new RuntimeException(t);
	}

	public static void patchHeldenErrorHandler(){
		try{
			InstrumentationEngine inst = new InstrumentationEngine("helden.Fehlermeldung");
			inst.addMethodInstrumentation("getWeitereInformationen",
					new InstrumentationEngine.MethodResultAModifier(ErrorHandler.class.getMethod("afterGetWeitereInformationen", String.class, Object.class)));
			inst.patchClass();
		}catch (Exception e){
			throw new RuntimeException(e);
		}
	}

	public static String afterGetWeitereInformationen(String s, Object handler){
		// Find throwable
		try{
			for (Field f: handler.getClass().getDeclaredFields()){
				if (f.getType().equals(Throwable.class)){
					f.setAccessible(true);
					Throwable t = (Throwable) f.get(handler);
					return getStacktrace(t) + s;
				}
			}
		}catch (Exception e){
			System.err.println(e.getClass().getName()+": "+e.getMessage());
		}
		return s;
	}

	private static String getStacktrace(Throwable t){
		if (t == null) return "";
		StringBuffer sb = new StringBuffer();
		t = t.getCause();
		while (t != null){
			sb.append("Caused by: "+t.getClass().getName()+": "+t.getMessage()+"\n");
			StackTraceElement[] st = t.getStackTrace();
			for (int i = 0; i < st.length; i++){
				sb.append(st[i]).append("\n");
			}
			sb.append("\n");
			t = t.getCause();
		}
		sb.append("\n");
		return sb.toString();
	}
}
