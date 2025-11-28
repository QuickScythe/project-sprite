package com.sprite.game.world.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.sprite.data.annotations.Nullable;
import com.sprite.data.TranslatableString;
import com.sprite.render.screen.GameScreen;
import com.sprite.render.ui.UI;
import com.sprite.render.ui.inventory.Inventory;
import com.sprite.resource.animations.Animation;
import com.sprite.resource.animations.AnimationDirector;
import com.sprite.resource.controllers.Controller;
import com.sprite.resource.entities.EntityType;
import com.sprite.resource.models.Model;
import com.sprite.data.utils.Utils;
import com.sprite.game.world.World;
import com.sprite.resource.ui.UIType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class Entity {

    public final Model model;
    public final int maxHealth;
    public final AnimationDirector director = new AnimationDirector();
    public final Controller controller;
    public final EntityType type;
    private final float width, height;
    private final Vector3 position;
    private Pathfinder pathfinder = null;
    private int health;
    private float speed;
    private TranslatableString name;
    private Entity target = null;
    private Pathfinder.Path path = null;
    private Body body;
    private final Inventory inventory;


    public Entity(EntityType type, float x, float y) {
        this.type = type;
        this.model = type.model.clone();
        this.health = type.health;
        this.maxHealth = type.health;
        this.speed = type.speed;
        this.position = new Vector3(x, y, 0);
        this.width = type.width;
        this.height = type.height;
        this.name = type.name;
        this.controller = type.controller.clone();
        this.inventory = new Inventory(type.inventoryUI);
        //todo replace with collision box
        this.body = new Body(position().x, position().y, width(), height());
        if (controller.input().id().equalsIgnoreCase("input:pathfinder")) {
            List<EntityType> targets = new ArrayList<>();
            JSONObject pathfinderData = controller.extra().optJSONObject("pathfinder", new JSONObject());
            if (pathfinderData.has("targets")) {
                JSONArray targetsArray = pathfinderData.getJSONArray("targets");
                for (int i = 0; i < targetsArray.length(); i++) {
                    String targetKey = targetsArray.getString(i);
                    EntityType target = Utils.resources().ENTITIES.load(targetKey);
                    if (target != null) targets.add(target);
                }
            }
            float maxJumpHeight = (float) pathfinderData.optDouble("maxJumpHeight", 1_000_000.0);
            pathfinder = new Pathfinder(targets, maxJumpHeight);

        }
        for (Animation animation : model.animations) {
            this.director.animations.register(animation.name, animation);
        }
    }

    public void speed(float speed) {
        this.speed = speed;
    }

    public @Nullable Pathfinder pathfinder() {
        return pathfinder;
    }

    public void name(TranslatableString name) {
        this.name = name;
    }

    public void render(GameScreen screen) {
        director.render(screen.sprite(), this, position.x, position.y, width, height);

        Vector3 prevNode = null;
        if (path == null)
            return;

        screen.sprite().end();
        screen.shape().setProjectionMatrix(screen.camera().combined);
        screen.shape().begin(ShapeRenderer.ShapeType.Line);
        screen.shape().setColor(Color.RED);
        for (Vector3 node : path.waypoints) {
            screen.shape().circle(node.x, node.y, 4);
            if (prevNode != null) {
                screen.shape().line(prevNode.x, prevNode.y, node.x, node.y);
            }
            prevNode = node;
        }
        screen.shape().end();
        screen.sprite().begin();

    }


    public void applyForce(float fx, float fy) {

        if (body == null) return;
        body.ax += fx;
        body.ay += fy;
    }

    public void setVelocity(float vx, float vy) {
        if (body == null) return;
        body.vx = vx;
        body.vy = vy;
    }

    public void jump(float impulse){
        if (body == null) return;
        if (onGround()){
            body.vy += impulse;
            body.onGround = false;
        }
    }

    public float getVelX() {
        return body.vx;
    }

    public float getVelY() {
        return body.vy;
    }

    public boolean onGround() {
        return body.onGround;
    }

    public void update(World world, float delta) {

        physics(world, delta);

        if (pathfinder() != null) {
            path = pathfinder.path(world, this);

            return;
        }
    }

    private void physics(World world, float delta) {
        if (body == null) return;

        // Apply gravity
        body.ay += world.gravity();

        // Integrate velocity
        body.vx += body.ax * delta;
        body.vy += body.ay * delta;

        // Damping (air drag) - apply primarily to horizontal motion
        body.vx *= world.linearDamping();
        // Avoid damping vertical velocity to keep gravity and jumps responsive

        // Integrate position
        body.x += body.vx * delta;
        body.y += body.vy * delta;

        // Ground collision with simple plane at y = worldFloorY
        body.onGround = false;
        if (body.y < world.floor()) {
            body.y = world.floor();
            if (body.vy < 0) {
                body.vy = -body.vy * world.restitution();
            }
            // When on ground, apply additional friction and zero tiny vertical velocity
            if (Math.abs(body.vy) < 0.01f) body.vy = 0f;
            body.onGround = true;
            body.vx *= world.groundFriction();
        }

        // Reset per-step acceleration
        body.ax = 0;
        body.ay = 0;

        // Sync entity position
        position().set(body.x, body.y, 0);
    }

    public int health() {
        return health;
    }

    public void health(int health) {
        this.health = Math.min(health, maxHealth);
    }

    public TranslatableString name() {
        return name;
    }

    public float width() {
        return width;
    }

    public float height() {
        return height;
    }

    public Vector3 position() {
        return position;
    }

    public float speed() {
        return speed;
    }

    public EntityType type() {
        return type;
    }

    public Inventory inventory() {
        return inventory;
    }

    public static class Position {
        private float x, y;

        public Position(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public float x() {
            return x;
        }

        public float y() {
            return y;
        }

        public void set(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public void x(float x) {
            this.x = x;
        }

        public void y(float y) {
            this.y = y;
        }
    }

    private static class Body {
        float x, y;
        float vx, vy;
        float ax, ay;
        boolean onGround;
        float width, height;

        Body(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    public class Pathfinder {

        private static final float CELL_SIZE = 16f;
        private static final int MAX_EXPANSIONS = 5000; // safety cap to avoid runaway search

        public final List<EntityType> targets;
        private Path lastPath = null;
        private float lastTargetX = Float.NaN;
        private float lastTargetY = Float.NaN;
        private int maxJumpCells = Integer.MAX_VALUE;

        public Pathfinder(List<EntityType> targets, float maxJumpHeight) {
            this.targets = targets;
            if (maxJumpHeight <= 0 || Float.isInfinite(maxJumpHeight)) {
                this.maxJumpCells = Integer.MAX_VALUE;
            } else {
                this.maxJumpCells = Math.max(0, Math.round(maxJumpHeight / CELL_SIZE));
            }
        }

        public Entity target() {
            return Entity.this.target;
        }

        public void target(Entity entity) {
            Entity.this.target = entity;
        }

        public Path path(World world, Entity entity) {
            if (target == null)
                findNewTarget(world, entity);
            if (target == null) return null;

            // If target hasn't moved significantly and we have a path, reuse it
            if (lastPath != null && Math.hypot(target.position().x - lastTargetX, target.position().y - lastTargetY) < CELL_SIZE * 0.25f) {
                return lastPath;
            }

            // Compute new path using A*
            Vector3 start = entity.position();
            Vector3 goal = target.position();
            Path computed = aStar(start.x, start.y, goal.x, goal.y);
            if (computed == null || computed.isEmpty()) {
                // Fallback to direct line path
                Path fallback = new Path();
                fallback.addWaypoint(start.x, start.y);
                fallback.addWaypoint(goal.x, goal.y);
                lastPath = fallback;
            } else {
                lastPath = computed;
            }
            lastTargetX = goal.x;
            lastTargetY = goal.y;
            return lastPath;
        }

        private void findNewTarget(World world, Entity exclude) {
            float closestDistance = Float.MAX_VALUE;
            Entity closestEntity = null;
            for (Entity other : world.entities) {
                if (other == exclude) continue;
                if (targets.stream().noneMatch(t -> t.name.key().equalsIgnoreCase(other.type().name.key()))) continue;
                float distance = exclude.position().dst(other.position());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestEntity = other;
                }
            }
            Entity.this.target = closestEntity;

        }

        private Path aStar(float sx, float sy, float gx, float gy) {
            int sxi = Math.round(sx / CELL_SIZE);
            int syi = Math.round(sy / CELL_SIZE);
            int gxi = Math.round(gx / CELL_SIZE);
            int gyi = Math.round(gy / CELL_SIZE);
            final int startYCell = syi;

            Node start = new Node(sxi, syi, 0, 0, null);
            Node goal = new Node(gxi, gyi, 0, 0, null);

            PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));
            Map<Node, Float> bestG = new HashMap<>();
            Set<Node> closed = new HashSet<>();

            start.f = heuristic(start, goal);
            open.add(start);
            bestG.put(start, 0f);

            int expansions = 0;
            while (!open.isEmpty() && expansions < MAX_EXPANSIONS) {
                Node current = open.poll();
                if (current.x == goal.x && current.y == goal.y) {
                    return reconstruct(current);
                }
                if (!closed.add(current)) continue;
                expansions++;

                // 8-directional neighbors
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (dx == 0 && dy == 0) continue;
                        int nx = current.x + dx;
                        int ny = current.y + dy;

                        // Enforce max jump height relative to start Y
                        if (ny - startYCell > maxJumpCells) continue;

                        Node neighbor = new Node(nx, ny, 0, 0, null);
                        if (closed.contains(neighbor)) continue;

                        float stepCost = (dx == 0 || dy == 0) ? 1f : 1.41421356f; // straight vs diagonal
                        float tentativeG = bestG.get(current) + stepCost;
                        Float knownG = bestG.get(neighbor);
                        if (knownG == null || tentativeG < knownG) {
                            neighbor.parent = current;
                            neighbor.g = tentativeG;
                            neighbor.f = tentativeG + heuristic(neighbor, goal);
                            bestG.put(neighbor, tentativeG);
                            open.add(neighbor);
                        }
                    }
                }
            }
            return null; // failed to find path within limits
        }

        private float heuristic(Node a, Node b) {
            // Euclidean distance
            int dx = a.x - b.x;
            int dy = a.y - b.y;
            return (float) Math.sqrt(dx * dx + dy * dy);
        }

        private Path reconstruct(Node end) {
            ArrayList<float[]> rev = new ArrayList<>();
            Node n = end;
            while (n != null) {
                rev.add(new float[]{n.x * CELL_SIZE, n.y * CELL_SIZE});
                n = n.parent;
            }
            Path p = new Path();
            for (int i = rev.size() - 1; i >= 0; i--) {
                float[] xy = rev.get(i);
                p.addWaypoint(xy[0], xy[1]);
            }
            return p;
        }

        public void clear() {
            Entity.this.path = null;
            Entity.this.target = null;
        }

        public static class Path {
            private final List<Vector3> waypoints = new ArrayList<>();
            private int index = 0;

            public void addWaypoint(float x, float y) {
                waypoints.add(new Vector3(x, y, 0));
            }

            public boolean isEmpty() {
                return waypoints.isEmpty();
            }

            public int size() {
                return waypoints.size();
            }

            public int index() {
                return index;
            }

            public void reset() {
                index = 0;
            }

            public boolean hasNext() {
                return index < waypoints.size();
            }

            public Vector3 next() {
                return waypoints.get(index++);
            }

            public Vector3 peek() {
                return waypoints.get(Math.min(index, waypoints.size() - 1));
            }

            public List<Vector3> points() {
                return waypoints;
            }
        }

        private class Node {
            final int x, y;
            float g; // cost from start
            float f; // g + heuristic
            Node parent;

            Node(int x, int y, float g, float f, Node parent) {
                this.x = x;
                this.y = y;
                this.g = g;
                this.f = f;
                this.parent = parent;
            }

            @Override
            public int hashCode() {
                return (x * 73856093) ^ (y * 19349663);
            }

            @Override
            public boolean equals(Object o) {
                if (!(o instanceof Node)) return false;
                Node n = (Node) o;
                return n.x == x && n.y == y;
            }
        }

    }
}
