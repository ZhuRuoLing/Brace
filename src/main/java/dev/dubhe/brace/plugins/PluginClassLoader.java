package dev.dubhe.brace.plugins;

//import dev.dubhe.brace.BraceServer;

public class PluginClassLoader extends ClassLoader {
    private PluginClassLoader(ClassLoader parent) {
        super(parent);
    }

    public static PluginClassLoader getInstance(ClassLoader parent){
        return new  PluginClassLoader(parent);
    }

    public final Class<?> defineClazz(String name, byte[] b, int off, int len) throws ClassFormatError {
        return super.defineClass(name, b, off, len);
    }

    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        return super.findClass(name);
        /*
        for (PluginInstance value : BraceServer.getPluginManager().pluginFiles.values()) {
            if (value.classMap.containsKey(name)) return value.classMap.get(name);
        }
        throw new ClassNotFoundException();
        */
    }
}
