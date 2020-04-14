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
package toniarts.openkeeper.game.event.events;

import toniarts.openkeeper.world.object.ObjectControl;
import toniarts.openkeeper.game.event.IEvent;

/**
 *
 * @author ArchDemon
 */
public class AddObjectEvent implements IEvent {

    private final ObjectControl objectControl;

    public AddObjectEvent(ObjectControl objectControl) {
        this.objectControl = objectControl;
    }

    public ObjectControl getObjectControl() {
        return objectControl;
    }

}
