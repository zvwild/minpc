package dev.zvwild.minpc.plugin.util;

import java.lang.reflect.Field;

public final class ReflectionUtils {

    private ReflectionUtils() {
    }

    public static Object readField(String name, Object instance) {
        try {
            Field field = instance.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(instance);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

}
