package com.sprite.render;

public interface RenderPipeline {

    public abstract void background();
    public abstract void backgroundShaders();
    public abstract void foreground();
    public abstract void foregroundShader();
    public abstract void ui();
    public abstract void uiShader();

}
