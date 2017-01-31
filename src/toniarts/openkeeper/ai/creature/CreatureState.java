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
import toniarts.openkeeper.tools.convert.map.Variable.MiscVariable.MiscType;
import toniarts.openkeeper.world.animation.AnimationControl.AnimationType;
import toniarts.openkeeper.world.creature.CreatureControl;

/**
 * State machine for creature AI. TODO: needs to be hierarchial so that this
 * class doesn't grow to be millions of lines
 *
 * @author Toni Helenius <helenius.toni@gmail.com>
 */
public enum CreatureState implements State<CreatureControl> {

    IDLE() {

        @Override
        public void enter(CreatureControl entity) {

            // Should we flee or attack
            if (entity.shouldFleeOrAttack()) {
                return;
            }

            entity.getAnimation().play(AnimationType.IDLE);
            // Idling is the last resort
            entity.unassingCurrentTask();
            if (!entity.findStuffToDo(entity)) {
                entity.navigateToRandomPoint();
            }
        }

        @Override
        public void update(CreatureControl entity) {

            // Should we flee or attack
            if (entity.shouldFleeOrAttack()) {
                return;
            }

            if (!entity.findStuffToDo(entity)) {
                
                
                if (entity.isStopped() && entity.getAnimation().isCycleDone()) {
                    entity.getAnimation().stop();
                    if (!entity.getAnimation().isEnabled()) {
                        entity.navigateToRandomPoint();
                    }
                }
            }
        }

        @Override
        public void exit(CreatureControl entity) {
            entity.getAnimation().stop();
        }

        @Override
        public boolean onMessage(CreatureControl entity, Telegram telegram) {
            return true;
        }
    },
    WANDER() {

        @Override
        public void enter(CreatureControl entity) {
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
            entity.die();
        }

        @Override
        public void update(CreatureControl entity) {
            float time = entity.getVariable(MiscType.DEAD_BODY_DIES_AFTER_SECONDS);
            if (entity.getTimeInState() > time) {
                entity.removeCreature();
            }
        }

        @Override
        public void exit(CreatureControl entity) {

        }

        @Override
        public boolean onMessage(CreatureControl entity, Telegram telegram) {
            return true;
        }
    }, SLAPPED {

        @Override
        public void enter(CreatureControl entity) {
            entity.stop();
            entity.getAnimation().play(AnimationType.FALLBACK);
        }

        @Override
        public void update(CreatureControl entity) {
            if (!entity.getAnimation().isEnabled()) {
                entity.getStateMachine().revertToPreviousState();
                //entity.getStateMachine().changeState(IDLE);
            }
        }

        @Override
        public void exit(CreatureControl entity) {

        }

        @Override
        public boolean onMessage(CreatureControl entity, Telegram telegram) {
            return true;
        }
    }, WORK {

        @Override
        public void enter(CreatureControl entity) {
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

                // If we have too much gold, drop it to the treasury
                if (entity.isTooMuchGold()) {
                    if (!entity.dropGoldToTreasury()) {
                        entity.dropGold();
                    }
                }
            }

            // Check validity
            // If we have some pocket money left, we should return it to treasury
            if (!entity.isAssignedTaskValid() && !entity.dropGoldToTreasury()) {
                entity.getStateMachine().changeState(IDLE);
            }
            
            if (entity.getAssignedTask() != null && !entity.getAnimation().isEnabled()) {
                entity.getAnimation().play(AnimationType.WORK);
            }
            
            if (entity.isStopped() && entity.getAnimation().getType() == AnimationType.WORK 
                    && entity.isAssignedTaskValid()) {
                // Different work based reactions
                entity.getAssignedTask().executeTask(entity);
            }

        }

        @Override
        public void exit(CreatureControl entity) {
            entity.getAnimation().stop();
        }

        @Override
        public boolean onMessage(CreatureControl entity, Telegram telegram) {
            return true;
        }

    }, FIGHT {

        @Override
        public void enter(CreatureControl entity) {
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
            entity.followTarget(entity.getFollowTarget());
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

            // Don't let the target wander too far off
            if (entity.isStopped() && !entity.getFollowTarget().isStopped() 
                    && entity.getDistanceToCreature(entity.getFollowTarget()) > 2.5f) {
                entity.followTarget(entity);
            } else if (entity.isStopped()) {
                entity.navigateToRandomPointAroundTarget(entity.getFollowTarget(), 2);
            }
        }

        @Override
        public void exit(CreatureControl entity) {
            entity.resetFollowTarget();
            entity.stop();
        }

        @Override
        public boolean onMessage(CreatureControl entity, Telegram telegram) {
            return true;
        }

    },
    ENTERING_DUNGEON {

        @Override
        public void enter(CreatureControl entity) {
            entity.getAnimation().play(AnimationType.ENTERING);
        }

        @Override
        public void update(CreatureControl entity) {
            if (!entity.getAnimation().isEnabled()) {
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
    PICKED_UP {

        @Override
        public void enter(CreatureControl entity) {

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
            entity.stop();
            entity.unassingCurrentTask();
            
            entity.getAnimation().play(AnimationType.DYING);
            entity.showUnitFlower(Integer.MAX_VALUE);
        }

        @Override
        public void update(CreatureControl entity) {
            
            float time = entity.getVariable(MiscType.CREATURE_DYING_STATE_DURATION_SECONDS);
            if (entity.getTimeInState() > time) {
                entity.getStateMachine().changeState(DEAD);
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

        @Override
        public void enter(CreatureControl entity) {
            entity.getAnimation().play(AnimationType.STUNNED);
        }

        @Override
        public void update(CreatureControl entity) {
            float time = entity.getVariable(MiscType.CREATURE_STUNNED_TIME_SECONDS);
            if (entity.getTimeInState() > time) {
                entity.getStateMachine().changeState(IDLE);
            }
            //float time = gameState.getLevelVariable(MiscType.CREATURE_STUNNED_EFFECT_DELAY_SECONDS);
        }

        @Override
        public void exit(CreatureControl entity) {

        }

        @Override
        public boolean onMessage(CreatureControl entity, Telegram telegram) {
            return true;
        }
    };
}
