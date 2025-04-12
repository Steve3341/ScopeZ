package scopez;

public class MagnifierWrapper {
    public native boolean nativeInit();
    public native void nativeSetResolution(int width, int height);
    public native void nativeSetWindowShape(boolean circular);
    public native void nativeSetRefreshRate(int refreshRate);
    public native void nativeSetZoom(double zoomFactor);
    public native void nativeShowWindow();
    public native void nativeHideWindow();
    public native void nativeMoveWindow(int offsetX, int offsetY);
    public native void nativeDispose();

}

