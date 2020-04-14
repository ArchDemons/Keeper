/*
 * Copyright (C) 2014-2020 OpenKeeper
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
package toniarts.openkeeper.game.event;

/**
 * An Event whose processing may be interrupted when the event has been handled.
 *
 * A Dispatcher implementation MUST check to determine if an Event is marked as
 * stopped after each listener is called. If it is then it should return
 * immediately without calling any further Listeners.
 *
 * @author ArchDemon
 */
public interface IStoppableEvent {

    /**
     * Is propagation stopped?
     *
     * This will typically only be used by the Dispatcher to determine if the
     * previous listener halted propagation.
     *
     * @return True if the Event is complete and no further listeners should be
     * called. False to continue calling listeners.
     */
    public boolean isPropagationStopped();
}
