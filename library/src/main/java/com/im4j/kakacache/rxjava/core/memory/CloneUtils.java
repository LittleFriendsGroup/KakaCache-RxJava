package com.im4j.kakacache.rxjava.core.memory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 对象深度克隆
 * @version imkarl 2016-09
 */
public class CloneUtils {
    private CloneUtils() {}

    /**
     * 无需进行复制的特殊类型数组
     */
    private static final Class[] needlessCloneClasses = new Class[]{String.class,Boolean.class,Character.class,Byte.class,Short.class,
            Integer.class,Long.class,Float.class,Double.class,Void.class,Object.class,Class.class
    };
    /**
     * 判断该类型对象是否无需复制
     * @param c 指定类型
     * @return 如果不需要复制则返回真，否则返回假
     */
    private static boolean isNeedlessClone(Class c){
        if(c.isPrimitive()){//基本类型
            return true;
        }
        for(Class tmp:needlessCloneClasses){//是否在无需复制类型数组里
            if(c.equals(tmp)){
                return true;
            }
        }
        return false;
    }

    /**
     * 尝试创建新对象
     * @param value 原始对象
     * @return 新的对象
     * @throws IllegalAccessException
     */
    private static Object createObject(Object value) throws IllegalAccessException{
        try {
            return value.getClass().newInstance();
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            throw e;
        }
    }

    /**
     * 复制对象数据
     * @param value 原始对象
     * @param level 复制深度。小于0为无限深度，即将深入到最基本类型和Object类级别的数据复制；
     * 大于0则按照其值复制到指定深度的数据，等于0则直接返回对象本身而不进行任何复制行为。
     * @return 返回复制后的对象
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    public static <T> T clone(T value,int level) throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException{
        Object cloneValue;
        if(value==null){
            return null;
        }
        if(level==0){
            return value;
        }
        Class c = value.getClass();
        if(isNeedlessClone(c)){
            return value;
        }
        level--;
        if(value instanceof Collection){//复制新的集合
            Collection tmp = (Collection)c.newInstance();
            for(Object v:(Collection)value){
                tmp.add(clone(v,level));//深度复制
            }
            cloneValue = tmp;
        }
        else if(c.isArray()){//复制新的Array
            //首先判断是否为基本数据类型
            if(c.equals(int[].class)){
                int[] old = (int[])value;
                cloneValue = (T) Arrays.copyOf(old, old.length);
            }
            else if(c.equals(short[].class)){
                short[] old = (short[])value;
                cloneValue = (T) Arrays.copyOf(old, old.length);
            }
            else if(c.equals(char[].class)){
                char[] old = (char[])value;
                cloneValue = (T) Arrays.copyOf(old, old.length);
            }
            else if(c.equals(float[].class)){
                float[] old = (float[])value;
                cloneValue = (T) Arrays.copyOf(old, old.length);
            }
            else if(c.equals(double[].class)){
                double[] old = (double[])value;
                cloneValue = (T) Arrays.copyOf(old, old.length);
            }
            else if(c.equals(long[].class)){
                long[] old = (long[])value;
                cloneValue = (T) Arrays.copyOf(old, old.length);
            }
            else if(c.equals(boolean[].class)){
                boolean[] old = (boolean[])value;
                cloneValue = (T) Arrays.copyOf(old, old.length);
            }
            else if(c.equals(byte[].class)){
                byte[] old = (byte[])value;
                cloneValue = (T) Arrays.copyOf(old, old.length);
            }
            else {
                Object[] old = (Object[])value;
                Object[] tmp = Arrays.copyOf(old, old.length, old.getClass());
                for(int i = 0;i<old.length;i++){
                    tmp[i] = clone(old[i],level);
                }
                cloneValue = (T) tmp;
            }
        }
        else if(value instanceof Map){//复制新的MAP
            Map tmp = (Map)c.newInstance();
            Map org = (Map)value;
            for(Object key:org.keySet()){
                tmp.put(key, clone(org.get(key),level));//深度复制
            }
            cloneValue = (T) tmp;
        }
        else {
            Object tmp = createObject(value);
            if(tmp==null){//无法创建新实例则返回对象本身，没有克隆
                return value;
            }
            Set<Field> fields = new HashSet<Field>();
            while(c!=null&&!c.equals(Object.class)){
                fields.addAll(Arrays.asList(c.getDeclaredFields()));
                c = c.getSuperclass();
            }
            for(Field field:fields){
                if(!Modifier.isFinal(field.getModifiers())){//仅复制非final字段
                    field.setAccessible(true);
                    field.set(tmp, clone(field.get(value),level));//深度复制
                }
            }
            cloneValue = (T) tmp;
        }
        return (T) cloneValue;
    }

    /**
     * 浅表复制对象
     * @param value 原始对象
     * @return 复制后的对象，只复制一层
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    public static <T> T clone(T value) throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException{
        return clone(value, 1);
    }

    /**
     * 深度复制对象
     * @param value 原始对象
     * @return 复制后的对象
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    public static <T> T deepClone(T value) throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException{
        return clone(value, -1);
    }
}
