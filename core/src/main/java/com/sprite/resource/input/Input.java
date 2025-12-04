package com.sprite.resource.input;

import com.sprite.resource.Resource;

public class Input {

    private final String id;

    public Input(Resource resource){
        this.id = resource.location.toString();
    }

    public String id() {
        return id;
    }



}
