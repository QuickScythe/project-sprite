package com.sprite.data;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Locale;

public interface JsonSerializable {

    default JSONObject json() {
        JSONObject json = new JSONObject();
        Class<?> currentClass = this.getClass();

        while (currentClass != null) {
            for (Field field : currentClass.getDeclaredFields()) {
                if (Modifier.isPrivate(field.getModifiers())) continue;

                field.setAccessible(true);
                try {
                    Object value = field.get(this);
                    if (value == null)
                        continue;
                    if (value instanceof JsonSerializable data) {
                        if (json.has(field.getName())) {
                            continue; // Skip if already processed to prevent infinite recursion
                        }
                        value = data.json();
                    }
                    json.put(field.getName(), value);

                } catch (Exception e) {
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        return json;
    }

    default void fromJson(JSONObject json) {
        Class<?> currentClass = this.getClass();

        while (currentClass != null) {
            for (var field : currentClass.getDeclaredFields()) {
                if (Modifier.isPrivate(field.getModifiers())) continue;
                field.setAccessible(true);
                if (json.has(field.getName())) {
                    try {
                        Object value = json.get(field.getName());
                        Class<?> fieldType = field.getType();
                        if (JsonSerializable.class.isAssignableFrom(fieldType) && value instanceof JSONObject obj) {
                            value = fieldType.getDeclaredConstructor().newInstance();
                            ((JsonSerializable) value).fromJson(obj);
                        }
                        if(double.class.isAssignableFrom(fieldType) && value instanceof Number number){
                            value = number.doubleValue();
                        }
                        if(fieldType.isAnonymousClass()){

//                            System.out.println("Anonymous class detected for field: " + field.getName() + ", value: " + value);

                        }
                        setField(field, this, value);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to set field: " + field.getName(), e);
                    }
                }
            }
            currentClass = currentClass.getSuperclass();
        }
    }

//    default Object get(String fieldName) {
//        try {
//            var field = this.getClass().getDeclaredField(fieldName);
//            field.setAccessible(true);
//            return field.get(this);
//        } catch (NoSuchFieldException | IllegalAccessException e) {
//            throw new RuntimeException("Failed to get field: " + fieldName, e);
//        }
//    }

    default void setField(Field field, Object target, Object value) throws IllegalAccessException {
        if (field.getType().isEnum() && value instanceof String) {
            @SuppressWarnings("unchecked")
            Class<? extends Enum> enumClass = (Class<? extends Enum>) field.getType();
            field.set(target, convertStringToEnum((String) value, enumClass));
        } else if (field.getType() == boolean.class || field.getType() == Boolean.class) {
            if (value instanceof String) {
                field.set(target, Boolean.parseBoolean((String) value));
            } else {
                field.set(target, value);
            }
        } else if (field.getType() == double.class || field.getType() == Double.class){
            if (value instanceof String string) {
                field.set(target, Double.parseDouble(string));
            } else {
                field.set(target, value);
            }
        } else {
            field.set(target, value);
        }
    }

    default <E extends Enum<E>> E convertStringToEnum(String value, Class<E> enumClass) {
        if (value == null) return null;
        return Enum.valueOf(enumClass, value.toUpperCase(Locale.ROOT));
    }


}
