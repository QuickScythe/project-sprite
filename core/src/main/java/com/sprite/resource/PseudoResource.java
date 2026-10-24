package com.sprite.resource;

import org.json.JSONObject;

public class PseudoResource extends Resource {

    public PseudoResource(JSONObject data) {
        super(data);
    }

    @Override
    public Type type() {
        return Type.DATA;
    }
}
