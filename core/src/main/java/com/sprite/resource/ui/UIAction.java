package com.sprite.resource.ui;

import com.badlogic.gdx.Gdx;
import com.sprite.resource.Resource;
import com.sprite.resource.ResourceMeta;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Method;

public class UIAction {

    Class<?> clazz;
    Method method;
    JSONObject[] args;

    public UIAction(JSONObject data) {
        load(data);

    }

    public UIAction(Resource resource) {
        if (!resource.type().equals(Resource.Type.DATA))
            throw new IllegalStateException("Action resource must be of type DATA");
        ResourceMeta.Json data = (ResourceMeta.Json) resource.data.data();
        load(data.get());
    }

    private void load(JSONObject data){
        String className = data.optString("class", "com.sprite.data.utils.ActionUtils");
        try {
            clazz = Class.forName(className);
            JSONArray methodArgTypes = data.optJSONArray("arguments", new JSONArray());
            Class<?>[] argTypes = new Class<?>[methodArgTypes.length()];
            args = new JSONObject[methodArgTypes.length()];
            for (int i = 0; i < methodArgTypes.length(); i++) {
                if (!(methodArgTypes.get(i) instanceof JSONObject))
                    throw new IllegalStateException("Invalid argument type: " + methodArgTypes.get(i));
                JSONObject argType = methodArgTypes.getJSONObject(i);
                argTypes[i] = JSONObject.class;
                if (argType.get("value") instanceof JSONObject json)
                    args[i] = json;
                else args[i] = new JSONObject().put("value", argType.get("value"));
            }
            method = clazz.getMethod(data.getString("method"), argTypes);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Class not found: " + className);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Method not found: " + data.getString("method"));
        }
        Gdx.app.log("UI", "Loading UI Action method: " + method.getName());

    }

    public void invoke() {
        try {
            method.invoke(null, (Object[]) args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke method: " + method.getName(), e);
        }
    }
}
