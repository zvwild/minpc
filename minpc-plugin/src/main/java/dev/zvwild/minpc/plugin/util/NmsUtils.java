package dev.zvwild.minpc.plugin.util;

public final class NmsUtils {

    private NmsUtils() {
    }

    private static int currentId = Integer.MAX_VALUE;

    public static int getNewEntityId() {
        return currentId--;
    }

    public static byte convertToAngle(float v) {
        return (byte) ((int) (v * 256.0f / 360.0f));
    }

}
