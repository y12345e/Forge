/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Forge Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package forge.game.phase;

import forge.game.GameState;

/**
 * <p>
 * Handles "until end of combat" effects and "at end of combat" hardcoded triggers.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class EndOfCombat extends Phase {

    /** Constant <code>serialVersionUID=3035250030566186842L</code>. */
    private static final long serialVersionUID = 3035250030566186842L;

    public EndOfCombat(final GameState game) { super(game); }
    
} // end class EndOfCombat
