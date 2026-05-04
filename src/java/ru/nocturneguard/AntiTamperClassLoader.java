package ru.nocturneguard;

import ru.nocturneguard.J2C.Native;

import java.lang.instrument.ClassDefinition;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;

@Native
public final class AntiTamperClassLoader {
    private AntiTamperClassLoader() {}

    public static void hook() {
        try {
            ClassLoader cl = AntiTamperClassLoader.class.getClassLoader();
            if (cl instanceof URLClassLoader) {
                URLClassLoader ucl = (URLClassLoader) cl;
                for (URL u : ucl.getURLs()) {
                    String s = u.toString().toLowerCase();
                    if (s.contains("unwanted-tool") || s.contains("debug")) {
                        System.err.println("AntiTamperClassLoader: suspicious classpath entry " + s);
                        Runtime.getRuntime().halt(1);
                    }
                }
            }
        } catch (Throwable t) {

        }
    }
}
