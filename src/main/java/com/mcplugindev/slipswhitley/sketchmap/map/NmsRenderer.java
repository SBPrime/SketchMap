package com.mcplugindev.slipswhitley.sketchmap.map;

import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapView;

/**
 * This class contains all methods needed to store the images in the native NMS
 * format
 *
 * @author SBPrime
 */
public final class NmsRenderer {
    private static class NmsAccessor {

        private final Field centerX;
        private final Field centerZ;
        private final Field map;
        private final Field scale;
        private final Field colors;
        private final Method flagDirty;
        private final Method update;
        private final Field nmsData;

        private NmsAccessor(Field fNmsData,
                Field fCenterX, Field fCenterZ, Field fMap, Field fScale, Field fColors, Method mFlagDirty, Method mUpdate) {
            nmsData = fNmsData;
            centerX = fCenterX;
            centerZ = fCenterZ;
            map = fMap;
            scale = fScale;
            colors = fColors;
            
            flagDirty = mFlagDirty;
            update = mUpdate;
        }

        private NmsAccessor() {
            this(null, null, null, null, null, null, null, null);
        }

        private boolean isNms(MapView mapView) {
            Object nms = get(nmsData, mapView);
            if (nms == null) {
                return false;
            }
            
            return Objects.equals(DIMENSION, get(map, nms));
        }

        private byte[] getData(MapView mapView) {
            Object nms = get(nmsData, mapView);
            if (nms == null) {
                return new byte[0];
            }
            
            return (byte[])get(colors, nms);
        }

        private boolean setData(MapView mapView, byte[] imgData) {
            try {
                Object nms = get(nmsData, mapView);
                if (nms == null) {
                    return false;
                }
                
                centerX.set(nms, (int)0);
                centerZ.set(nms, (int)0);
                map.set(nms, DIMENSION);
                scale.set(nms, (byte)1);
                System.arraycopy(imgData, 0, (byte[])colors.get(nms), 0, imgData.length);
                
                flagDirty.invoke(nms, (int)0, (int)0);                
                flagDirty.invoke(nms, (int)127, (int)127);
                
                update.invoke(nms);
                
                return true;
            } catch (IllegalArgumentException ex) {
                return false;
            } catch (IllegalAccessException ex) {
                return false;
            } catch (InvocationTargetException ex) {
                return false;
            }
        }
    }

    private static final byte DIMENSION = 99; 
    
    private static final MessageDigest MD5;
    
    private static final NmsAccessor NULL = new NmsAccessor();

    private static final ConcurrentMap<String, NmsAccessor> s_accessor
            = new ConcurrentHashMap<>();

    static {
        MessageDigest md5 = null;
        try {        
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {            
        }
        
        MD5 = md5;
    }
    
    static boolean setImage(final MapView mapView, BufferedImage img) {
        final Class<? extends MapView> mvCls = mapView.getClass();

        if (mvCls == null) {
            return false;
        }

        String mvClsName = mvCls.getCanonicalName();
        NmsAccessor accessor = s_accessor.compute(mvClsName, (i, j)
                -> createAccessor(j, mvCls, mapView));

        if (accessor == null || Objects.equals(accessor, NULL)) {
            return false;
        }

        final byte[] imgData = MapPalette.imageToBytes(img);
        boolean updateData;
        if (accessor.isNms(mapView)) {
            updateData = !md5(accessor.getData(mapView)).equals(md5(imgData));
        } else {
            updateData = true;
        }
        
        if (updateData) {
            return accessor.setData(mapView, imgData);
        }
        
        return true;
    }

    private static NmsAccessor createAccessor(NmsAccessor currentAccessor,
            Class<? extends MapView> mvCls, MapView mv) {
        if (currentAccessor != null) {
            return currentAccessor;
        }

        Field fWorldMap = getField(mvCls, "worldMap");
        if (fWorldMap == null) {
            return NULL;
        }
        Object worldMap = get(fWorldMap, mv);
        if (worldMap == null) {
            return NULL;
        }
        Class<?> nmsMap = worldMap.getClass();

        Field fCenterX = getField(nmsMap, "centerX", int.class);
        Field fCenterZ = getField(nmsMap, "centerZ", int.class);
        Field fMap = getField(nmsMap, "map", byte.class);
        Field fScale = getField(nmsMap, "scale", byte.class);
        Field fColors = getField(nmsMap, "colors", byte[].class);
        Method mFlagDirty = getMethod(nmsMap, "flagDirty", void.class, new Class<?>[]{int.class, int.class});
        Method mUpdate = null;

        Class<?> nmsMapSuper = nmsMap.getSuperclass();
        if (nmsMapSuper != null) {
            for (Method mi : nmsMapSuper.getDeclaredMethods()) {
                if (!void.class.getCanonicalName().equals(mi.getReturnType().getCanonicalName())) {
                    continue;
                }

                Parameter[] params = mi.getParameters();
                if (params != null && params.length > 0) {
                    continue;
                }

                if ((mi.getModifiers() & Modifier.PUBLIC) == Modifier.PUBLIC) {
                    mi.setAccessible(true);
                    mUpdate = mi;
                    break;
                }
            }
        }

        if (fCenterX == null || fCenterZ == null
                || fMap == null || fScale == null
                || fColors == null
                || mFlagDirty == null || mUpdate == null) {
            return NULL;
        }

        return new NmsAccessor(fWorldMap,
                fCenterX, fCenterZ,
                fMap, fScale, fColors,
                mFlagDirty, mUpdate
        );
    }

    private static Method getMethod(Class<?> cls, String name,
            Class<?> cResult, Class<?>[] cParams) {
        try {
            Method m = cls.getDeclaredMethod(name, cParams);

            if (!cResult.getCanonicalName().equals(m.getReturnType().getCanonicalName())) {
                return null;
            }

            m.setAccessible(true);
            return m;
        } catch (NoSuchMethodException ex) {
            return null;
        } catch (SecurityException ex) {
            return null;
        }
    }

    private static Field getField(Class<?> cls, String fieldName) {
        try {
            Field result = cls.getDeclaredField(fieldName);
            result.setAccessible(true);

            return result;
        } catch (NoSuchFieldException ex) {
            return null;
        } catch (SecurityException ex) {
            return null;
        }
    }

    private static Field getField(Class<?> cls, String name, Class<?> expectedType) {
        try {
            Field result = cls.getDeclaredField(name);

            if (!expectedType.getCanonicalName().equals(result.getType().getCanonicalName())) {
                return null;
            }

            result.setAccessible(true);

            return result;
        } catch (NoSuchFieldException ex) {
            return null;
        } catch (SecurityException ex) {
            return null;
        }
    }

    private static Object get(Field f, Object o) {
        try {
            return f.get(o);
        } catch (IllegalArgumentException ex) {
            return null;
        } catch (IllegalAccessException ex) {
            return null;
        }
    }
    
    private static String md5(byte[] data) {
        MD5.reset();
        return new BigInteger(MD5.digest(data)).toString(16);
    }
}
