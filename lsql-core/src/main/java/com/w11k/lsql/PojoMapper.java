package com.w11k.lsql;

import com.google.common.collect.Maps;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.util.Map;

@Deprecated
public class PojoMapper<T> {

    private final static Map<Class<?>, PojoMapper<?>> CACHE = Maps.newHashMap();

    @SuppressWarnings("unchecked")
    public static <T> PojoMapper<T> getFor(Class<T> clazz) {
        if (!CACHE.containsKey(clazz)) {
            CACHE.put(clazz, new PojoMapper<Object>((Class<Object>) clazz));
        }
        return (PojoMapper<T>) CACHE.get(clazz);
    }

    private final Class<T> pojoClass;

    private final Map<String, PropertyDescriptor> propertyDescriptors = Maps.newHashMap();

    private final Constructor<T> constructor;

    public PojoMapper(Class<T> pojoClass) {

        // Find constructor
        this.constructor = getConstructor(pojoClass);
        this.pojoClass = pojoClass;

        // Extract property names
        PropertyDescriptor[] descs;
        try {
            descs = Introspector.getBeanInfo(pojoClass).getPropertyDescriptors();
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }

        for (PropertyDescriptor desc : descs) {
            Class<?> declaringClass = desc.getReadMethod().getDeclaringClass();
            if (declaringClass.equals(Object.class)) {
                continue;
            }
            this.propertyDescriptors.put(desc.getName(), desc);
        }
    }

    public T newInstance() {
        try {
            return this.constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Object getValue(T instance, String fieldName) {
        PropertyDescriptor descriptor = this.propertyDescriptors.get(fieldName);
        try {
            setPropertyAccessible(descriptor);
            return descriptor.getReadMethod().invoke(instance);
        } catch (Exception e) {
            throw new RuntimeException("field name: " + fieldName, e);
        }
    }

    public void setValue(T instance, String fieldName, Object value) {
        PropertyDescriptor descriptor = this.propertyDescriptors.get(fieldName);
        try {
            setPropertyAccessible(descriptor);
            descriptor.getWriteMethod().invoke(instance, value);
        } catch (Exception e) {
            throw new RuntimeException("field name: " + fieldName, e);
        }
    }

    public Class<?> getTypeOfField(String fieldName) {
        return this.propertyDescriptors.get(fieldName).getPropertyType();
    }

    public Row pojoToRow(T pojo) {
        try {
            Row row = new Row();
            for (PropertyDescriptor descriptor : this.propertyDescriptors.values()) {
                setPropertyAccessible(descriptor);
                Object value = descriptor.getReadMethod().invoke(pojo);
                row.put(descriptor.getName(), value);
            }
            return row;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public T rowToPojo(Row row) {
        T pojo = newInstance();
        assignRowToPojo(row, pojo);
        return pojo;
    }

    public void assignRowToPojo(Row row, T pojo) {
        for (PropertyDescriptor descriptor : this.propertyDescriptors.values()) {
            Object value = row.get(descriptor.getName());
            setValue(pojo, descriptor.getName(), value);
        }
    }

    /*
    public void checkConformity(Map<String, Converter> converters) {
        String logClassName = this.pojoClass.getCanonicalName();

        // Check SQL -> Java
        for (String columnName : converters.keySet()) {
            Converter converter = converters.get(columnName);

            // missing field
            PropertyDescriptor descriptor = this.propertyDescriptors.get(columnName);
            if (descriptor == null) {
                throw new IllegalArgumentException(
                        logClassName + " is missing field '" + columnName + "' of type '" + converter.getJavaType().getCanonicalName() + "'"
                );
            }
            // wrong type in field
            else if (!convertPrimitiveClassToWrapperClass(
                    descriptor.getPropertyType()).isAssignableFrom(converter.getJavaType())) {
                throw new IllegalArgumentException(
                        "Field " + logClassName + "#" + columnName + " has the wrong type: '" + descriptor.getPropertyType().getCanonicalName() + "'. Expected: '" + converter.getJavaType().getCanonicalName() + "'"
                );
            }
        }

        // Check Java -> SQL
        for (String field : this.propertyDescriptors.keySet()) {
            if (converters.keySet().contains(field)) {
                continue;
            }

            // superfluous field
            if (!converters.keySet().contains(field)) {
                throw new IllegalArgumentException(
                        logClassName + " has superfluous field '" + field + "'"
                );
            }
        }
    }
    */

    private Constructor<T> getConstructor(Class<T> pojoClass) {
        try {
            Constructor<T> constructor = pojoClass.getConstructor();
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }
            return constructor;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private void setPropertyAccessible(PropertyDescriptor desc) {
        try {
            if (!desc.getReadMethod().isAccessible()) {
                desc.getReadMethod().setAccessible(true);
            }
            if (!desc.getWriteMethod().isAccessible()) {
                desc.getWriteMethod().setAccessible(true);
            }
        } catch (SecurityException e) {
            throw new RuntimeException("Property read/write methods must be accessible", e);
        }
    }

}
