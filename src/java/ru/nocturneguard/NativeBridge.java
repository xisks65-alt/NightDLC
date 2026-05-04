package ru.nocturneguard;

public final class NativeBridge {
    private NativeBridge() {}

    public static void loadNative() {
        try {
            //System.loadLibrary("nocturne_native");
        } catch (UnsatisfiedLinkError e) {

        }
    }


    public static native boolean nativeEnvironmentCheck();
    public static native void onLoaderStarted();
    public static native void onTamperDetected(String reason);
}
