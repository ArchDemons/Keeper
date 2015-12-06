/*
 * Copyright (C) 2014-2015 OpenKeeper
 *
 * OpenKeeper is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenKeeper is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenKeeper.  If not, see <http://www.gnu.org/licenses/>.
 */
package toniarts.openkeeper.world;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import toniarts.openkeeper.tools.convert.AssetsConverter;
import toniarts.openkeeper.tools.convert.map.Creature.ResourceEnum;
import toniarts.openkeeper.tools.convert.map.KwdFile;
import toniarts.openkeeper.tools.convert.map.Thing;

/**
 *
 * @author ArchDemon
 */
public class GameCreature extends Node {

    private Thing.Creature info;
    private static KwdFile kwdFile;
    private int artResoureceId = 0;
    private AssetManager assetManager;
    private BulletAppState bulletAppState;
    private List<Vector3f> waypoints;
    private float progress = 0;
    private Vector3f start;
    private Vector3f lockAt = Vector3f.UNIT_Z;
    private static final Logger logger = Logger.getLogger(GameCreature.class.getName());
    private PathFinder finder;

    public GameCreature(BulletAppState bulletAppState, AssetManager assetManager, Thing.Creature creature, KwdFile kwdFile) {
        this.info = creature;

        if (GameCreature.kwdFile == null) {
            GameCreature.kwdFile = kwdFile;
        }

        this.assetManager = assetManager;
        this.bulletAppState = bulletAppState;

        this.loadModel();

        this.waypoints = new ArrayList<>();
        this.finder = new PathFinder(kwdFile);
    }

    private void loadModel() {
        this.detachAllChildren();
        String modelName = this.getModelFileName();
        Node model = (Node) assetManager.loadModel(AssetsConverter.MODELS_FOLDER + "/" + modelName + ".j3o");
        model.setLocalTranslation(
                info.getPosX() * MapLoader.TILE_WIDTH - MapLoader.TILE_WIDTH / 2f,
                info.getPosZ() * MapLoader.TILE_HEIGHT,
                info.getPosY() * MapLoader.TILE_WIDTH - MapLoader.TILE_WIDTH / 2f);

        AnimControl animControl = (AnimControl) model.getChild(0).getControl(AnimControl.class);
        if (animControl != null) {
            AnimChannel channel = animControl.createChannel();
            channel.setAnim("anim");
            channel.setLoopMode(LoopMode.Loop);

            // Don't batch animated objects, seems not to work
            model.setBatchHint(Spatial.BatchHint.Never);
        }

        //Geometry geom = (Geometry) model.getChild(0);
        model.getChild(0).addControl(new RigidBodyControl(0));

        bulletAppState.getPhysicsSpace().add(model.getChild(0));

        this.attachChild(model);
    }

    private String getModelFileName() {
        try {
            artResoureceId = 0;
            return kwdFile.getCreature(info.getCreatureId()).getResource(ResourceEnum.animWalk).getName();
        } catch (Exception e) {
            throw new RuntimeException("model not found");
        }
    }

    public void setArtResourceId(int index) {
        artResoureceId = index;
        this.loadModel();
    }

    public int getArtResourceId() {
        return artResoureceId;
    }

    @Override
    public void updateLogicalState(float tpf) {
        super.updateLogicalState(tpf);

        if (!waypoints.isEmpty()) {
            if (progress == 0) {
                this.start = this.getChild(0).getLocalTranslation();
                this.getChild(0).rotate(0, getAngle(lockAt, waypoints.get(0).subtract(start).normalize()), 0);
                lockAt = waypoints.get(0).subtract(start).normalize();
            }
            progress += tpf / 30;
            Vector3f position = FastMath.interpolateLinear(progress, start, waypoints.get(0));
            this.getChild(0).setLocalTranslation(position);

            if (progress > 1) {
                waypoints.remove(0);
                progress = 0;
            }
        }
    }

    private float getAngle(Vector3f from, Vector3f to) {

        float result = from.angleBetween(to);
        int q1 = this.getQuadrant(from), q2 = this.getQuadrant(to);

        return 0;
    }

    private int getQuadrant(Vector3f vector) {
        int quad = 1;

        if (vector.x < 0 && vector.z > 0) {
            quad = 2;
        } else if (vector.x < 0 && vector.z < 0) {
            quad = 3;
        } else if (vector.x > 0 && vector.z < 0) {
            quad = 4;
        }
        return quad;
    }
}
