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

import com.jme3.scene.control.Control;
import toniarts.openkeeper.tools.convert.map.Creature;

/**
 * An interface for handling our animations, callbacks
 *
 * @author Toni Helenius <helenius.toni@gmail.com>
 */
public interface AnimationControl extends Control {

    /**
     * On animation stop
     *
     */
    public void onAnimationStop();

    //public boolean isAnimationCycleDone();

    /**
     * Main animation cycle is done
     * @param count number of cycles
     */
    public void onAnimationCycleDone(int count);

    /**
     * Should we stop the animation
     * @return yes or not
     */
    public boolean isStopAnimation();

    /**
     * Set stop the animation
     */
    //public void animationStop();

    //public boolean isAnimationPlaying();

    //public Creature.AnimationType getAnimationTypePlaying();
}
