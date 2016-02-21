/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package toniarts.openkeeper.world.tree;

import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Spatial;
import java.util.List;

/**
 *
 * @author ArchDemon
 */
public class QuadTree {
    private static int SIZE = 6;
    private final QuadNode root;

    // define a quadtree extends as width and height, define quadtree depth.
    public QuadTree(final String name, final float x, final float y, final float z) {
        int dept = (int) Math.min(x / SIZE / 2, z / SIZE / 2);
        root = new QuadNode(name, Vector3f.ZERO, new Vector3f(x, y, z), dept);
    }

    // insert a GameObject at the quadtree
    public void attachChild(final Spatial spatial, final int x, final int y) {
        root.addChild(spatial, x, y);
    }

    public boolean detachChild(final int x, final int y) {
        return root.removeChild(x, y);
    }

    // detachAllChildren the quadtree
    public void detachAllChildren() {
        root.detachAllChildren();
    }

    public Spatial getRootNode() {
        return (Spatial) root;
    }

    public Spatial getChild(final int x, final int y) {
        return root.getChild(x, y);
    }

    public List<Spatial> getVisibleSpatial(Camera camera) {

        return root.getVisibleSpatial(camera);
    }
}
