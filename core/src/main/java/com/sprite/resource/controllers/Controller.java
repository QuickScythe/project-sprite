package com.sprite.resource.controllers;

import com.sprite.resource.Resource;
import com.sprite.resource.ResourceMeta;
import com.sprite.resource.input.Input;
import com.sprite.utils.Resources;
import com.sprite.utils.Utils;
import org.json.JSONObject;

public class Controller implements Cloneable {

    Input input;
    JSONObject json;

    public Controller(Resource resource){
        if(!resource.type().equals(Resource.Type.DATA)) throw new IllegalStateException("Controller resource must be of type DATA");
        ResourceMeta.Json data = (ResourceMeta.Json) resource.data.data();
        input = Utils.resources().INPUTS.load(data.get().getString("input"));
        json = new JSONObject();
        for(String key : data.get().keySet()){
            if(key.equalsIgnoreCase("input")) continue;
            json.put(key, data.get().get(key));
        }
    }

    public Input input(){
        return input;
    }

    @Override
    public Controller clone() {
        try {
            Controller clone = (Controller) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public JSONObject extra() {
        return json;
    }
}
