package com.sprite.game.world.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.sprite.data.annotations.Nullable;
import com.sprite.data.TranslatableString;
import com.sprite.input.InputAction;
import com.sprite.input.InputSystem;
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
        if(controller.input().id().equalsIgnoreCase("input:player")) {
            handlePlayerInput(screen);
        }

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

    private void handlePlayerInput(GameScreen screen) {
        if (!screen.ui().isOpen()) {
            boolean left = InputSystem.i().isActionPressed(InputAction.MOVE_LEFT);
            boolean right = InputSystem.i().isActionPressed(InputAction.MOVE_RIGHT);
            boolean jumpJustPressed = InputSystem.i().isActionJustPressed(InputAction.JUMP);

            // Horizontal movement: A/D (or Left/Right)
            float dirX = 0f;
            if (left) dirX -= 1f;
            if (right) dirX += 1f;

            // Apply horizontal velocity based on the entity's speed while preserving current vertical velocity
            float targetVx = (dirX * 1) * speed();
            setVelocity(targetVx, getVelY());

            // Jump on W (or Up) just pressed, if the entity can jump
            if (jumpJustPressed && onGround()) {
                // Basic jump impulse; keep consistent with existing debug usage (500)
                jump(15f);
            }
        }
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

//        if (pathfinder() != null) {
//            path = pathfinder.path(world, this);
//
//            return;
//        }
    }

    private void physics(World world, float delta) {
        if (body == null) return;

        // Apply gravity
        body.ay += world.gravity();

        // Integrate velocity
        // Remove delta-time scaling: advance one fixed step per frame
        body.vx += body.ax;
        body.vy += body.ay;

        // Damping (air drag) - apply primarily to horizontal motion
        body.vx *= world.linearDamping();
        // Avoid damping vertical velocity to keep gravity and jumps responsive

        // Integrate and resolve collisions against TileWorld
        body.onGround = false;

        int ts = world.tileSize();

        // Move along X and resolve
        float dx = body.vx;
        if (dx != 0f) {
            float newX = body.x + dx;
            if (!collides(world, newX, body.y, body.width, body.height)) {
                body.x = newX;
            } else {
                // Snap to tile boundary depending on direction
                if (dx > 0) {
                    // moving right: clamp to left edge of blocking tile
                    int maxTileY = (int) Math.floor((body.y + body.height - 0.001f) / ts);
                    int minTileY = (int) Math.floor((body.y + 0.001f) / ts);
                    int tileX = (int) Math.floor((body.x + body.width - 0.001f) / ts);
                    // scan tiles along vertical span to find first solid just to the right
                    int stopTileX = tileX;
                    for (int ty = minTileY; ty <= maxTileY; ty++) {
                        int tx = (int) Math.floor((body.x + body.width + dx) / ts);
                        if (world.isSolid(world.getTileId(tx, ty))) {
                            stopTileX = tx;
                            break;
                        }
                    }
                    body.x = stopTileX * ts - body.width;
                } else {
                    // moving left: clamp to right edge of blocking tile
                    int maxTileY = (int) Math.floor((body.y + body.height - 0.001f) / ts);
                    int minTileY = (int) Math.floor((body.y + 0.001f) / ts);
                    int tileX = (int) Math.floor((body.x + 0.001f) / ts);
                    int stopTileX = tileX;
                    for (int ty = minTileY; ty <= maxTileY; ty++) {
                        int tx = (int) Math.floor((body.x + dx) / ts);
                        if (world.isSolid(world.getTileId(tx, ty))) {
                            stopTileX = tx;
                            break;
                        }
                    }
                    body.x = (stopTileX + 1) * ts;
                }
                body.vx = 0f;
            }
        }

        // Move along Y and resolve (sets onGround when landing)
        float dy = body.vy;
        if (dy != 0f) {
            float newY = body.y + dy;
            if (!collides(world, body.x, newY, body.width, body.height)) {
                body.y = newY;
            } else {
                if (dy > 0) {
                    // moving up: hit ceiling
                    int maxTileX = (int) Math.floor((body.x + body.width - 0.001f) / ts);
                    int minTileX = (int) Math.floor((body.x + 0.001f) / ts);
                    int tileY = (int) Math.floor((body.y + body.height - 0.001f) / ts);
                    int stopTileY = tileY;
                    for (int tx = minTileX; tx <= maxTileX; tx++) {
                        int ty = (int) Math.floor((body.y + body.height + dy) / ts);
                        if (world.isSolid(world.getTileId(tx, ty))) {
                            stopTileY = ty;
                            break;
                        }
                    }
                    body.y = stopTileY * ts - body.height;
                    body.vy = 0f;
                } else {
                    // moving down: land on ground
                    int maxTileX = (int) Math.floor((body.x + body.width - 0.001f) / ts);
                    int minTileX = (int) Math.floor((body.x + 0.001f) / ts);
                    int tileY = (int) Math.floor((body.y + 0.001f) / ts);
                    int stopTileY = tileY;
                    for (int tx = minTileX; tx <= maxTileX; tx++) {
                        int ty = (int) Math.floor((body.y + dy) / ts);
                        if (world.isSolid(world.getTileId(tx, ty))) {
                            stopTileY = ty;
                            break;
                        }
                    }
                    body.y = (stopTileY + 1) * ts;
                    if (body.vy < 0) {
                        body.vy = -body.vy * world.restitution();
                        if (Math.abs(body.vy) < 0.01f) body.vy = 0f;
                    } else {
                        body.vy = 0f;
                    }
                    body.onGround = true;
                }
            }
        }

        // Apply ground friction if grounded after Y resolution
        if (body.onGround) {
            body.vx *= world.groundFriction();
        }

        // Reset per-step acceleration
        body.ax = 0;
        body.ay = 0;

        // Sync entity position
        position().set(body.x, body.y, 0);
    }

    private static boolean collides(World world, float x, float y, float w, float h) {
        // Determine tile range overlapped by AABB
        int ts = world.tileSize();

        int minTx = (int) Math.floor((x + 0.001f) / ts);
        int maxTx = (int) Math.floor((x + w - 0.001f) / ts);
        int minTy = (int) Math.floor((y + 0.001f) / ts);
        int maxTy = (int) Math.floor((y + h - 0.001f) / ts);
        for (int tx = minTx; tx <= maxTx; tx++) {
            for (int ty = minTy; ty <= maxTy; ty++) {
                if (world.isSolid(world.getTileId(tx, ty))) return true;
            }
        }
        return false;
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

        // Pathfinding uses the world's tile size for cells so it aligns with TileWorld
        private static final int MAX_EXPANSIONS = 5000; // safety cap to avoid runaway search

        public final List<EntityType> targets;
        private Path lastPath = null;
        private float lastTargetX = Float.NaN;
        private float lastTargetY = Float.NaN;
        // Maximum jump height in world units; converted to tiles when generating neighbors
        private float maxJumpHeightWorld = Float.POSITIVE_INFINITY;

        public Pathfinder(List<EntityType> targets, float maxJumpHeight) {
            this.targets = targets;
            if (maxJumpHeight > 0 && !Float.isInfinite(maxJumpHeight)) {
                this.maxJumpHeightWorld = maxJumpHeight;
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
            float cellSize = world.tileSize();
            if (lastPath != null && Math.hypot(target.position().x - lastTargetX, target.position().y - lastTargetY) < cellSize * 0.25f) {
                return lastPath;
            }

            // Compute new path using A*
            Vector3 start = entity.position();
            Vector3 goal = target.position();
            Path computed = aStar(world, entity, start.x, start.y, goal.x, goal.y);
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

        private Path aStar(World world, Entity entity, float sx, float sy, float gx, float gy) {
            final int ts = world.tileSize();
            int sxi = (int) Math.floor(sx / ts);
            int syi = (int) Math.floor(sy / ts);
            int gxi = (int) Math.floor(gx / ts);
            int gyi = (int) Math.floor(gy / ts);
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
                    return reconstruct(world, current);
                }
                if (!closed.add(current)) continue;
                expansions++;

                // Generate neighbors using TileWorld solidity with gravity and jump capabilities
                for (Node neighbor : generateNeighbors(world, entity, current, startYCell)) {
                    if (closed.contains(neighbor)) continue;
                    float tentativeG = bestG.get(current) + neighborCost(current, neighbor);
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
            return null; // failed to find path within limits
        }

        private float heuristic(Node a, Node b) {
            // Manhattan for grid with gravity works well
            int dx = Math.abs(a.x - b.x);
            int dy = Math.abs(a.y - b.y);
            return dx + dy;
        }

        private Path reconstruct(World world, Node end) {
            final int ts = world.tileSize();
            ArrayList<float[]> rev = new ArrayList<>();
            Node n = end;
            while (n != null) {
                rev.add(new float[]{n.x * ts, n.y * ts});
                n = n.parent;
            }
            Path p = new Path();
            for (int i = rev.size() - 1; i >= 0; i--) {
                float[] xy = rev.get(i);
                p.addWaypoint(xy[0], xy[1]);
            }
            return p;
        }

        private Iterable<Node> generateNeighbors(World world, Entity entity, Node current, int startYCell) {
            java.util.ArrayList<Node> result = new java.util.ArrayList<>(8);
            final int ts = world.tileSize();

            // Helper lambdas over tile coords
            java.util.function.BiFunction<Integer, Integer, Boolean> standable = (tx, ty) -> isStandableAt(world, entity, tx, ty);
            java.util.function.BiFunction<Integer, Integer, Boolean> fits = (tx, ty) -> fitsAt(world, entity, tx, ty);

            int cx = current.x;
            int cy = current.y;

            // Ensure current is valid; if not on ground, drop to ground directly
            if (!standable.apply(cx, cy)) {
                int landY = findFallLandingY(world, entity, cx, cy, 8);
                if (landY != Integer.MIN_VALUE) {
                    result.add(new Node(cx, landY, 0, 0, current));
                }
                return result;
            }

            // Walk left/right on same level if destination standable
            int nx = cx - 1;
            if (standable.apply(nx, cy)) result.add(new Node(nx, cy, 0, 0, current));
            else {
                // If stepping into air, allow falling to first landing at that x
                if (fits.apply(nx, cy)) {
                    int land = findFallLandingY(world, entity, nx, cy, 8);
                    if (land != Integer.MIN_VALUE) result.add(new Node(nx, land, 0, 0, current));
                }
            }
            nx = cx + 1;
            if (standable.apply(nx, cy)) result.add(new Node(nx, cy, 0, 0, current));
            else {
                if (fits.apply(nx, cy)) {
                    int land = findFallLandingY(world, entity, nx, cy, 8);
                    if (land != Integer.MIN_VALUE) result.add(new Node(nx, land, 0, 0, current));
                }
            }

            // Step up and simple jump up: try up to maxJumpCells tiles higher, limited breadth (±1 x)
            int maxUpTiles = (int) Math.floor(maxJumpHeightWorld / ts);
            if (maxUpTiles <= 0 || maxUpTiles > 8) maxUpTiles = 3; // default conservative cap
            int maxUp = Math.min(maxUpTiles, 3); // keep conservative for minimal implementation
            for (int j = 1; j <= maxUp; j++) {
                // vertical clearance at current x
                if (fits.apply(cx, cy + j) && standable.apply(cx, cy + j)) {
                    result.add(new Node(cx, cy + j, 0, 0, current));
                    break;
                }
                // adjacent x
                if (fits.apply(cx - 1, cy + j) && standable.apply(cx - 1, cy + j)) {
                    result.add(new Node(cx - 1, cy + j, 0, 0, current));
                    break;
                }
                if (fits.apply(cx + 1, cy + j) && standable.apply(cx + 1, cy + j)) {
                    result.add(new Node(cx + 1, cy + j, 0, 0, current));
                    break;
                }
            }

            // Allow small step down (descending stairs)
            if (standable.apply(cx, cy - 1)) result.add(new Node(cx, cy - 1, 0, 0, current));

            return result;
        }

        private int findFallLandingY(World world, Entity entity, int tx, int startTy, int maxScan) {
            // Scan downward until we find first standable position
            for (int dy = 1; dy <= maxScan; dy++) {
                int ty = startTy - dy;
                if (isStandableAt(world, entity, tx, ty)) return ty;
                // if we've hit solid where body overlaps, stop
                if (!fitsAt(world, entity, tx, ty)) return Integer.MIN_VALUE;
            }
            return Integer.MIN_VALUE;
        }

        private boolean isStandableAt(World world, Entity entity, int tx, int ty) {
            if (!fitsAt(world, entity, tx, ty)) return false;
            // Require solid ground directly below any part of the entity's feet
            final int ts = world.tileSize();
            float wx = tx * ts;
            float wy = (ty * ts) - 1; // just below feet
            // Check across the entity's width
            float ex = wx + entity.width();
            for (float x = wx; x < ex; x += Math.min(ts, entity.width())) {
                if (world.isSolidAtWorld(x + 0.5f, wy)) return true;
            }
            // Also check the far edge
            if (world.isSolidAtWorld(ex - 0.5f, wy)) return true;
            return false;
        }

        private boolean fitsAt(World world, Entity entity, int tx, int ty) {
            final int ts = world.tileSize();
            float wx = tx * ts;
            float wy = ty * ts;
            // AABB vs solid tiles using sampling at tile grid
            int minTx = (int) Math.floor((wx + 0.001f) / ts);
            int maxTx = (int) Math.floor((wx + entity.width() - 0.001f) / ts);
            int minTy = (int) Math.floor((wy + 0.001f) / ts);
            int maxTy = (int) Math.floor((wy + entity.height() - 0.001f) / ts);
            for (int ix = minTx; ix <= maxTx; ix++) {
                for (int iy = minTy; iy <= maxTy; iy++) {
                    if (world.isSolid(world.getTileId(ix, iy))) return false;
                }
            }
            return true;
        }

        private float neighborCost(Node a, Node b) {
            // Base cost: horizontal/vertical move is 1 per tile; vertical up weighs slightly more
            int dx = Math.abs(a.x - b.x);
            int dy = Math.abs(a.y - b.y);
            float cost = dx + dy;
            if (b.y > a.y) cost += 0.25f * (b.y - a.y); // discourage excessive jumping
            return cost;
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
