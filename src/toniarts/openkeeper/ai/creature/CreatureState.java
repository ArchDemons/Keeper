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
package toniarts.openkeeper.ai.creature;

import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.jme3.math.Vector3f;
import toniarts.openkeeper.tools.convert.ConversionUtils;
import toniarts.openkeeper.tools.convert.map.Creature;
import toniarts.openkeeper.utils.Utils;
import toniarts.openkeeper.world.creature.CreatureControl;

/**
 * State machine for creature AI. TODO: needs to be hierarchial so that this
 * class doesn't grow to be millions of lines
 *
 * @author Toni Helenius <helenius.toni@gmail.com>
 */
public enum CreatureState implements State<CreatureControl> {

    IDLE() {

        Creature.AnimationType idleAnimation = null;

        @Override
        public void enter(CreatureControl entity) {
            entity.setStatusText(Utils.getMainTextResourceBundle().getString("2599"));

            // Should we flee or attack
            if (entity.shouldFleeOrAttack()) {
                return;
            }

            // Idling is the last resort
            entity.unassingCurrentTask();
            if (findStuffToDo(entity)) {
                return;
            }

            idleAnimation = entity.getAnimationIdle();
            entity.animationPlayState(idleAnimation);
            entity.animationStop();
        }

        private boolean findStuffToDo(CreatureControl entity) {

            // See if we should just follow
            if (entity.getParty() != null && !entity.getParty().isPartyLeader(entity)) {
                entity.setFollowTarget(entity.getParty().getPartyLeader());
                entity.getStateMachine().changeState(CreatureState.FOLLOW);
                return true;
            }

            // See if we have an objective
            if (entity.hasObjective() && entity.followObjective()) {
                entity.getStateMachine().changeState(CreatureState.WORK);
                return true;
            }

            // See lair need
            if (entity.needsLair() && !entity.hasLair() && entity.findLair()) {
                entity.getStateMachine().changeState(CreatureState.WORK);
                return true; // Found work
            }

            // See basic needs
            if (entity.hasLair() && entity.isNeedForSleep() && entity.goToSleep()) {
                entity.getStateMachine().changeState(CreatureState.WORK);
                return true; // Found work
            }

            // Find work
            if (entity.findWork() || (entity.isWorker() && entity.isTooMuchGold() && entity.dropGoldToTreasury())) {
                entity.getStateMachine().changeState(CreatureState.WORK);
                return true; // Found work
            }

            return false;
        }

        @Override
        public void update(CreatureControl entity) {

            // Should we flee or attack
            if (entity.shouldFleeOrAttack() || findStuffToDo(entity)) {
                return;
            }

            if (!entity.isAnimationPlaying()) {
                entity.navigateToRandomPoint();
            }
        }

        @Override
        public void exit(CreatureControl entity) {

        }

        @Override
        public boolean onMessage(CreatureControl entity, Telegram telegram) {
            return true;
        }
    },
    MOVE() {

        Creature.AnimationType moveAnimation;

        @Override
        public void enter(CreatureControl entity) {
            moveAnimation = Creature.AnimationType.WALK;
            entity.animationPlayState(Creature.AnimationType.WALK);
        }

        @Override
        public void update(CreatureControl entity) {
            // Should we flee or attack
            if (entity.shouldFleeOrAttack()) {
                return;
            }

            if (entity.isStopped()) {
                entity.animationStop();
            }

            if (!entity.isAnimationPlaying()) {
                entity.getStateMachine().revertToPreviousState();
            }
        }

        @Override
        public void exit(CreatureControl entity) {

        }

        @Override
        public boolean onMessage(CreatureControl entity, Telegram telegram) {
            return true;
        }

    },
    WANDER() {

        @Override
        public void enter(CreatureControl entity) {
            entity.setStatusText(Utils.getMainTextResourceBundle().getString("2628"));
//                    entity.wander();
        }

        @Override
        public void update(CreatureControl entity) {
//                    if (entity.idleTimeExceeded()) {
//                        entity.getStateMachine().changeState(IDLE);
//                    }
        }

        @Override
        public void exit(CreatureControl entity) {

        }

        @Override
        public boolean onMessage(CreatureControl entity, Telegram telegram) {
            return true;
        }

    },
    DEAD() {
        @Override
        public void enter(CreatureControl entity) {
            entity.setStatusText(Utils.getMainTextResourceBundle().getString("2598"));
            entity.die();
            entity.removeCreature();
        }

        @Override
        public void update(CreatureControl entity) {
//                    if (entity.idleTimeExceeded()) {
//                        entity.getStateMachine().changeState(IDLE);
//                    }
        }

        @Override
        public void exit(CreatureControl entity) {

        }

        @Override
        public boolean onMessage(CreatureControl entity, Telegram telegram) {
            return true;
        }
    },
    WORK {

        @Override
        public void enter(CreatureControl entity) {
            if (entity.getAssignedTask() != null) {
                entity.setStatusText(entity.getAssignedTask().getTooltip());
            }
            entity.navigateToAssignedTask();
        }

        @Override
        public void update(CreatureControl entity) {

            // Should we flee or attack
            if (entity.shouldFleeOrAttack()) {
                return;
            }

            // Check arrival
            if (entity.isAtAssignedTaskTarget()) {
                entity.executeAssignedTask();
            } else if (entity.isStopped()) {
                entity.navigateToAssignedTask();
            }

            // If we have too much gold, drop it to the treasury
            if (entity.isTooMuchGold() && !entity.dropGoldToTreasury()) {
                entity.dropGold();
            }

            // Check validity
            // If we have some pocket money left, we should return it to treasury
            if (!entity.isAssignedTaskValid()) {
                entity.getStateMachine().changeState(IDLE);
            }
        }

        @Override
        public void exit(CreatureControl entity) {
            entity.animationStop();
        }

        @Override
        public boolean onMessage(CreatureControl entity, Telegram telegram) {
            return true;
        }

    }, FIGHT {

        @Override
        public void enter(CreatureControl entity) {
            entity.setStatusText(Utils.getMainTextResourceBundle().getString("2651"));
            entity.unassingCurrentTask();
            CreatureControl attackTarget = entity.getAttackTarget();
            if (attackTarget != null && !entity.isWithinAttackDistance(attackTarget)) {
                entity.navigateToAttackTarget(attackTarget);
            }
        }

        @Override
        public void update(CreatureControl entity) {
            CreatureControl attackTarget = entity.getAttackTarget();
            if (attackTarget == null) {
                entity.getStateMachine().changeState(IDLE); // Nothing to do
                return;
            }

            // Should we flee or attack
            if (entity.shouldFleeOrAttack()) {
                return;
            }

            // If we have reached the target, stop and fight!
            if (entity.isWithinAttackDistance(attackTarget)) {

                // Attack!!
                entity.stop();
                entity.executeAttack(attackTarget);
            } else {
                entity.navigateToAttackTarget(attackTarget);
            }
        }

        @Override
        public void exit(CreatureControl entity) {

        }

        @Override
        public boolean onMessage(CreatureControl entity, Telegram telegram) {
            return true;
        }

    }, FOLLOW {

        @Override
        public void enter(CreatureControl entity) {
            entity.setStatusText(Utils.getMainTextResourceBundle().getString("2675"));
            //entity.navigateToRandomPointAroundTarget(entity.getFollowTarget(), 2);
            //entity.followTarget(entity.getFollowTarget());
        }

        @Override
        public void update(CreatureControl entity) {

            // See if we should follow
            if (entity.getFollowTarget() == null || entity.getFollowTarget().isIncapacitated()) {
                entity.getStateMachine().changeState(IDLE);
                return;
            }

            // Should we flee or attack
            if (entity.shouldFleeOrAttack()) {
                return;
            }

            // If leader has set a task, perform it
            if (entity.getAssignedTask() != null) {
                entity.getStateMachine().changeState(WORK);
                return;
            }

            // && entity.getDistanceToCreature(entity.getFollowTarget()) > 2.5f
            if (entity.isStopped() && !entity.getFollowTarget().isStopped()) {
                entity.navigateToRandomPointAroundTarget(entity.getFollowTarget(), 1);
            }
        }

        @Override
        public void exit(CreatureControl entity) {
            //entity.resetFollowTarget();
            //entity.stop();
        }

        @Override
        public boolean onMessage(CreatureControl entity, Telegram telegram) {
            return true;
        }

    },
    ENTRANCE {

        @Override
        public void enter(CreatureControl entity) {
            entity.setStatusText("");
            entity.loadEffect(entity.getCreature().getEntranceEffectId());
            entity.animationPlayState(Creature.AnimationType.ENTRANCE);
            entity.animationStop();
        }

        @Override
        public void update(CreatureControl entity) {
            if (!entity.isAnimationPlaying()) {
                entity.getStateMachine().changeState(IDLE);
            }
        }

        @Override
        public void exit(CreatureControl entity) {
            javax.vecmath.Vector3f temp = entity.getCreature().getAnimationOffsets(Creature.OffsetType.PORTAL_ENTRANCE);
            Vector3f offset = ConversionUtils.convertVector(temp);
            entity.getSpatial().move(offset);
        }

        @Override
        public boolean onMessage(CreatureControl entity, Telegram telegram) {
            return true;
        }

    },
    PICKED_UP {

        @Override
        public void enter(CreatureControl entity) {
            entity.setStatusText("");
            entity.animationPlayState(Creature.AnimationType.IN_HAND);
            entity.unassingCurrentTask();
            entity.stop();
            entity.setEnabled(false);
            // Remove from view
            entity.getSpatial().removeFromParent();
        }

        @Override
        public void update(CreatureControl entity) {

        }

        @Override
        public void exit(CreatureControl entity) {

        }

        @Override
        public boolean onMessage(CreatureControl entity, Telegram telegram) {
            return true;
        }

    }, FLEE {

        @Override
        public void enter(CreatureControl entity) {
            entity.setStatusText(Utils.getMainTextResourceBundle().getString("2658"));
            entity.unassingCurrentTask();
            entity.flee();
        }

        @Override
        public void update(CreatureControl entity) {
            if (!entity.shouldFleeOrAttack()) {
                entity.getStateMachine().changeState(IDLE);
            }
        }

        @Override
        public void exit(CreatureControl entity) {

        }

        @Override
        public boolean onMessage(CreatureControl entity, Telegram telegram) {
            return true;
        }

    }, UNCONSCIOUS {
        @Override
        public void enter(CreatureControl entity) {
            entity.setStatusText(Utils.getMainTextResourceBundle().getString("2655"));
            entity.animationPlayState(Creature.AnimationType.DIE);
            entity.showUnitFlower(Integer.MAX_VALUE);
            entity.animationStop();
            entity.stop();
            entity.unassingCurrentTask();
        }

        @Override
        public void update(CreatureControl entity) {
            if (entity.isAnimationCycleDone()) {
                entity.animationPlayState(Creature.AnimationType.DEATH_POSE);
            }
        }

        @Override
        public void exit(CreatureControl entity) {

        }

        @Override
        public boolean onMessage(CreatureControl entity, Telegram telegram) {
            return true;
        }

    }, STUNNED {
        float stunTime;

        @Override
        public void enter(CreatureControl entity) {
            entity.setStatusText(Utils.getMainTextResourceBundle().getString("2597"));
            entity.animationPlayState(Creature.AnimationType.STUNNED);
            stunTime = entity.getCreature().getAttributes().getStunDuration();
        }

        @Override
        public void update(CreatureControl entity) {
            if (stunTime <= 0) {
                entity.animationStop();
                entity.getStateMachine().changeState(IDLE);
                entity.setEnabled(true);
            } else {
                // FIXME need use tpf
                stunTime -= 0.25f;
            }
        }

        @Override
        public void exit(CreatureControl entity) {

        }

        @Override
        public boolean onMessage(CreatureControl entity, Telegram telegram) {
            return true;
        }
    },
    FALLBACK {

        @Override
        public void enter(CreatureControl entity) {
            entity.setStatusText("");
            entity.animationPlayState(Creature.AnimationType.FALLBACK);
            entity.animationStop();
        }

        @Override
        public void update(CreatureControl entity) {
            if (entity.getAnimationTypePlaying() == Creature.AnimationType.FALLBACK
                    && !entity.isAnimationPlaying()) {
                entity.animationPlayState(Creature.AnimationType.GET_UP);
                entity.animationStop();
            } else if (entity.getAnimationTypePlaying() == Creature.AnimationType.GET_UP
                    && !entity.isAnimationPlaying()) {
                Vector3f offset = ConversionUtils.convertVector(
                        entity.getCreature().getAnimationOffsets(Creature.OffsetType.FALL_BACK_GET_UP));
                entity.getSpatial().move(offset);
                entity.getStateMachine().changeState(IDLE);

            }
        }

        @Override
        public void exit(CreatureControl entity) {

        }

        @Override
        public boolean onMessage(CreatureControl entity, Telegram telegram) {
            return true;
        }
    },
    IMPRISONED {

        @Override
        public void enter(CreatureControl entity) {
            entity.setStatusText(Utils.getMainTextResourceBundle().getString("2674"));
        }

        @Override
        public void update(CreatureControl entity) {

        }

        @Override
        public void exit(CreatureControl entity) {

        }

        @Override
        public boolean onMessage(CreatureControl entity, Telegram telegram) {
            return true;
        }
    }, TORTURED {

        @Override
        public void enter(CreatureControl entity) {
            entity.setStatusText(Utils.getMainTextResourceBundle().getString("2635"));
        }

        @Override
        public void update(CreatureControl entity) {

        }

        @Override
        public void exit(CreatureControl entity) {

        }

        @Override
        public boolean onMessage(CreatureControl entity, Telegram telegram) {
            return true;
        }
    }, SLEEPING {

        @Override
        public void enter(CreatureControl entity) {
            entity.setStatusText(Utils.getMainTextResourceBundle().getString("2672"));
        }

        @Override
        public void update(CreatureControl entity) {
            if (entity.isAttacked() || entity.isEnoughSleep()) {
                entity.getStateMachine().changeState(IDLE);
            }
        }

        @Override
        public void exit(CreatureControl entity) {

        }

        @Override
        public boolean onMessage(CreatureControl entity, Telegram telegram) {
            return true;
        }
    }, RECUPERATING {

        @Override
        public void enter(CreatureControl entity) {
            entity.setStatusText(Utils.getMainTextResourceBundle().getString("2667"));
        }

        @Override
        public void update(CreatureControl entity) {
            if (entity.isFullHealth()) {
                entity.getStateMachine().changeState(IDLE);
            }
        }

        @Override
        public void exit(CreatureControl entity) {

        }

        @Override
        public boolean onMessage(CreatureControl entity, Telegram telegram) {
            return true;
        }
    }, DRAGGED {

        @Override
        public void enter(CreatureControl entity) {
            entity.setStatusText(Utils.getMainTextResourceBundle().getString("2655"));
            entity.animationPlayState(Creature.AnimationType.DRAGGED);
        }

        @Override
        public void update(CreatureControl entity) {

        }

        @Override
        public void exit(CreatureControl entity) {
            entity.animationStop();
        }

        @Override
        public boolean onMessage(CreatureControl entity, Telegram telegram) {
            return true;
        }
    }
}
