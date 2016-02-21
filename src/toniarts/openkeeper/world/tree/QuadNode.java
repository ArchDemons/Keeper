/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package toniarts.openkeeper.world.tree;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import toniarts.openkeeper.world.MapLoader;

/**
 *
 * @author ArchDemon
 */
public class QuadNode extends Node {

    private final BoundingBox bound;
    private final int dept;
    private static final Logger logger = Logger.getLogger(BoundingBox.class.getName());

    public QuadNode(final String name, final Vector3f min, final Vector3f max, int dept) {
        this(min, max, dept);
        super.setName(name);
    }

    public QuadNode(final Vector3f min, final Vector3f max, int dept) {
        bound = new BoundingBox(min, max);
        this.dept = dept;
        setModelBound(bound);

        if (this.dept <= 0) {
            return;
        }
        // create 4 child nodes as long as depth is still greater than 0
        Vector3f half = new Vector3f(FastMath.floor(max.x / 2),
                FastMath.floor(max.y / 2),
                FastMath.floor(max.z / 2));
        // top-left
        QuadNode node = new QuadNode(min, new Vector3f(half.x, max.y, half.z), dept - 1);
        attachChild(node);
        // top-right
        node = new QuadNode(new Vector3f(half.x, min.y, min.z),
                new Vector3f(max.x, max.y, half.z), dept - 1);
        attachChild(node);
        //bottom-left
        node = new QuadNode(new Vector3f(min.x, min.y, half.z),
                new Vector3f(half.x, max.y, max.z), dept - 1);
        attachChild(node);
        // bottom-right
        node = new QuadNode(new Vector3f(half.x, min.y, half.z), max, dept - 1);
        attachChild(node);
    }

    public BoundingBox getBound() {
        return bound;
    }

    public void addChild(final Spatial spatial, final int x, int y) {

        if (dept <= 0) {
            attachChild(spatial);
            return;
        }

        Vector3f pos = new Vector3f(x + MapLoader.TILE_WIDTH / 2, MapLoader.TILE_HEIGHT / 2, y + MapLoader.TILE_WIDTH / 2);
        for (Spatial child : getChildren()) {
            if (child instanceof QuadNode && ((QuadNode) child).bound.contains(pos)) {
                ((QuadNode) child).addChild(spatial, x, y);
                return;
            }
        }

        logger.log(Level.WARNING, "Spatial Point[{0},{1}] not added", new Object[]{x, y});
    }

    public boolean removeChild(final int x, final int y) {
        Spatial child = getChild(x, y);
        if (child != null) {
            return child.removeFromParent();
        }
        return false;
    }

    public Spatial getChild(final int x, final int y) {
        Vector3f pos = getChildPos(x, y);
        for (Spatial child : getChildren()) {
            if (dept > 0 && child instanceof QuadNode) {
                if (((QuadNode) child).bound.contains(pos)) {
                    return ((QuadNode) child).getChild(x, y);
                }
            } else if (child.getName().equals(x + "_" + y)) {
                return child;
            }
        }

        return null;
    }

    private Vector3f getChildPos(final int x, final int y) {
        return new Vector3f(x + MapLoader.TILE_WIDTH / 2, MapLoader.TILE_HEIGHT / 2, y + MapLoader.TILE_WIDTH / 2);
    }

    public List<Spatial> getVisibleSpatial(Camera camera) {

        ArrayList<Spatial> result = new ArrayList();
        for (Spatial spatial : getChildren()) {
            if (dept > 0) {
                QuadNode q = (QuadNode) spatial;
                if (camera.contains(q.getBound()) != Camera.FrustumIntersect.Outside) {
                    result.addAll(q.getVisibleSpatial(camera));
                }
            } else {
                result.add(spatial.clone());
            }
        }
        return result;
    }
}
