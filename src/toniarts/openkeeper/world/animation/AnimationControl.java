/*
 * Copyright (C) 2014-2016 OpenKeeper
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
package toniarts.openkeeper.world.animation;

import com.jme3.asset.AssetManager;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import java.util.ArrayList;
import java.util.List;
import toniarts.openkeeper.ai.creature.CreatureState;
import toniarts.openkeeper.tools.convert.map.ArtResource;
import toniarts.openkeeper.tools.convert.map.Creature;
import toniarts.openkeeper.utils.Utils;
import toniarts.openkeeper.world.creature.CreatureAttack;
import toniarts.openkeeper.world.creature.CreatureControl;

/**
 * An interface for handling our animations, callbacks
 *
 * @author Toni Helenius <helenius.toni@gmail.com>
 */
public class AnimationControl extends AbstractControl {

    public enum AnimationType {

        NONE, ENTERING, MOVE, WORK, IDLE, ATTACK, DYING, STUNNED, OTHER, FALLBACK;
    }

    private final Creature creature;
    private final AssetManager assetManager;
    private AnimationType type = AnimationType.NONE;
    private boolean needStop = false;
    private boolean cycleDone = false;
    private ArtResource animation;
    

    public AnimationControl(Creature creature, AssetManager assetManager) {
        this.enabled = false;
        this.creature = creature;
        this.assetManager = assetManager;
    }
    
    @Override
    protected void controlUpdate(float tpf) {
        if (spatial != null && animation != null) {
            cycleDone = false;
            AnimationLoader.playAnimation(spatial, animation, assetManager);
            animation = null;
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }
    
    public void onAnimationCycleDone() {
        cycleDone = true;
    }

    public boolean isCycleDone() {
        return cycleDone;
    }

    /**
     * On animation stop
     *
     */
    public void onAnimationStop() {
        enabled = false;
        needStop = false;
        type = AnimationType.NONE;
        System.out.println("AnimationStop");
    }

    /**
     * Should we stop the animation
     * @return stop or not
     */
    public boolean isNeedStop() {
        return needStop;
    }
    
    public void stop() {
        needStop = true;
    }

    public AnimationType getType() {
        return type;
    }

    public void play(AnimationType type) {
        if (enabled) {
            return;
        }
 
        switch (type) {
            case IDLE:
                List<ArtResource> idleAnimations = new ArrayList<>(3);
                if (creature.getAnimIdle1Resource() != null) {
                    idleAnimations.add(creature.getAnimIdle1Resource());
                }
                if (creature.getAnimIdle2Resource() != null) {
                    idleAnimations.add(creature.getAnimIdle2Resource());
                }
                animation = idleAnimations.get(0);
                if (idleAnimations.size() > 1) {
                    animation = Utils.getRandomItem(idleAnimations);
                }
                break;
                
            case STUNNED:
                animation = creature.getAnimGetUpResource();
                break;
                
            case ENTERING:
                animation = creature.getAnimEntranceResource();
                break;
                
            case MOVE:  
                animation = creature.getAnimWalkResource();
                break;
                
            case DYING:
                animation = creature.getAnimDieResource();
                break;

            case WORK:
                CreatureControl cc = spatial.getControl(CreatureControl.class);
                animation = cc.getAssignedTask().getTaskAnimation(cc);
                
            case ATTACK:
                cc = spatial.getControl(CreatureControl.class);
                CreatureAttack executeAttack = cc.getExecutingAttack();
                if (executeAttack != null && executeAttack.isPlayAnimation()) {
                    if (executeAttack.isMelee()) {
                        List<ArtResource> meleeAttackAnimations = new ArrayList<>(2);
                        if (creature.getAnimMelee1Resource() != null) {
                            meleeAttackAnimations.add(creature.getAnimMelee1Resource());
                        }
                        if (creature.getAnimMelee2Resource() != null) {
                            meleeAttackAnimations.add(creature.getAnimMelee2Resource());
                        }
                        ArtResource attackAnim = meleeAttackAnimations.get(0);
                        if (meleeAttackAnimations.size() > 1) {
                            attackAnim = Utils.getRandomItem(meleeAttackAnimations);
                        }
                        animation = attackAnim;
                    } else {
                        animation = creature.getAnimMagicResource();
                    }
                }                
                break;
        }
        
        if (animation != null) {
            enabled = true;
            this.type = type;
            System.out.println("AnimationPlay " + type.toString());
        }
    }
}
