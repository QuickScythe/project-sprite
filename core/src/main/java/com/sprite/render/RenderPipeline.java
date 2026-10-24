package com.sprite.render;

import com.sprite.render.lighting.LightingSystem;
import com.sprite.render.particle.ParticleSystem;
import com.sprite.render.screen.GameScreen;
import com.sprite.render.shader.ShaderLayer;

public class RenderPipeline {

    private ShaderLayer shaderLayer = new ShaderLayer();
    private LightingSystem lightingSystem = new LightingSystem();
    private ParticleSystem particleSystem = new ParticleSystem();

    private RenderPhase background = (screen, delta) -> {
    };
    private RenderPhase foreground = (screen, delta) -> {
    };
    private RenderPhase particles = (screen, delta) -> {
        if (particleSystem != null) {
            particleSystem.render(screen, delta);
        }
    };
    private RenderPhase shader = (screen, delta) -> {
        if (shaderLayer != null) {
            shaderLayer.render(screen, delta);
        }
    };
    private RenderPhase lights = (screen, delta) -> {
        if (lightingSystem != null) {
            lightingSystem.render(screen, delta);
        }
    };
    private RenderPhase postProcess = (screen, delta) -> {
    };
    private RenderPhase ui = (screen, delta) -> {
    };

    public RenderPhase background() {
        return background;
    }

    public RenderPhase foreground() {
        return foreground;
    }

    public RenderPhase particles() {
        return particles;
    }

    public RenderPhase shader() {
        return shader;
    }

    public RenderPhase lights() {
        return lights;
    }

    public RenderPhase postProcess() {
        return postProcess;
    }

    public RenderPhase ui() {
        return ui;
    }

    public void background(RenderPhase background) {
        this.background = background != null ? background : (screen, delta) -> {};
    }

    public void foreground(RenderPhase foreground) {
        this.foreground = foreground != null ? foreground : (screen, delta) -> {};
    }

    public void particles(RenderPhase particles) {
        this.particles = particles != null ? particles : (screen, delta) -> {};
    }

    public void shader(RenderPhase shader) {
        this.shader = shader != null ? shader : (screen, delta) -> {};
    }

    public void lights(RenderPhase lights) {
        this.lights = lights != null ? lights : (screen, delta) -> {};
    }

    public void postProcess(RenderPhase postProcess) {
        this.postProcess = postProcess != null ? postProcess : (screen, delta) -> {};
    }

    public void ui(RenderPhase ui) {
        this.ui = ui != null ? ui : (screen, delta) -> {};
    }

    public ShaderLayer shaderLayer() {
        return shaderLayer;
    }

    public void shaderLayer(ShaderLayer shaderLayer) {
        this.shaderLayer = shaderLayer;
    }

    public ShaderLayer getShaderLayer() {
        return shaderLayer;
    }

    public void setShaderLayer(ShaderLayer shaderLayer) {
        this.shaderLayer = shaderLayer;
    }

    public LightingSystem lightingSystem() {
        return lightingSystem;
    }

    public void lightingSystem(LightingSystem lightingSystem) {
        this.lightingSystem = lightingSystem;
    }

    public LightingSystem getLightingSystem() {
        return lightingSystem;
    }

    public void setLightingSystem(LightingSystem lightingSystem) {
        this.lightingSystem = lightingSystem;
    }

    public ParticleSystem particleSystem() {
        return particleSystem;
    }

    public void particleSystem(ParticleSystem particleSystem) {
        this.particleSystem = particleSystem;
    }

    public ParticleSystem getParticleSystem() {
        return particleSystem;
    }

    public void setParticleSystem(ParticleSystem particleSystem) {
        this.particleSystem = particleSystem;
    }

    public void update(float delta) {
        if (particleSystem != null) {
            particleSystem.update(delta);
        }
        if (lightingSystem != null) {
            lightingSystem.update(delta);
        }
    }

    public void render(GameScreen screen, float delta) {
        background.render(screen, delta);
        foreground.render(screen, delta);
        particles.render(screen, delta);
        lights.render(screen, delta);
        shader.render(screen, delta);
        postProcess.render(screen, delta);
        ui.render(screen, delta);
    }

    @FunctionalInterface
    public interface RenderPhase {

        void render(GameScreen screen, float delta);

    }

}
