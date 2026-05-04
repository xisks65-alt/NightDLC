/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.wh1tew1ndows.baritone;

import dev.wh1tew1ndows.baritone.api.IBaritone;
import dev.wh1tew1ndows.baritone.api.IBaritoneProvider;
import dev.wh1tew1ndows.baritone.api.cache.IWorldScanner;
import dev.wh1tew1ndows.baritone.api.command.ICommandSystem;
import dev.wh1tew1ndows.baritone.api.schematic.ISchematicSystem;
import dev.wh1tew1ndows.baritone.cache.WorldScanner;
import dev.wh1tew1ndows.baritone.command.CommandSystem;
import dev.wh1tew1ndows.baritone.command.ExampleBaritoneControl;
import dev.wh1tew1ndows.baritone.utils.schematic.SchematicSystem;

import java.util.Collections;
import java.util.List;

/**
 * @author Brady
 * @since 9/29/2018
 */
public final class BaritoneProvider implements IBaritoneProvider {

    private final Baritone primary;
    private final List<IBaritone> all;

    {
        this.primary = new Baritone();
        this.all = Collections.singletonList(this.primary);

        // Setup chat control, just for the primary instance
        new ExampleBaritoneControl(this.primary);
    }

    @Override
    public IBaritone getPrimaryBaritone() {
        return primary;
    }

    @Override
    public List<IBaritone> getAllBaritones() {
        return all;
    }

    @Override
    public IWorldScanner getWorldScanner() {
        return WorldScanner.INSTANCE;
    }

    @Override
    public ICommandSystem getCommandSystem() {
        return CommandSystem.INSTANCE;
    }

    @Override
    public ISchematicSystem getSchematicSystem() {
        return SchematicSystem.INSTANCE;
    }
}
