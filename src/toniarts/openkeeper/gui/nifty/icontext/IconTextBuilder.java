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
package toniarts.openkeeper.gui.nifty.icontext;

import de.lessvoid.nifty.builder.ControlBuilder;

/**
 * Builder for the IconText
 *
 * @author Toni Helenius <helenius.toni@gmail.com>
 */
public class IconTextBuilder extends ControlBuilder {

    /**
     * Build a new icon text
     *
     * @param id id
     * @param icon the icon image file
     * @param text the text you want to display
     * @param onClick onClick callback method
     */
    public IconTextBuilder(final String id, final String icon, final String text, final String onClick) {
        super(id, "iconText");

        parameter("icon", icon);
        parameter("text", text);
        parameter("click", onClick);
    }
}
