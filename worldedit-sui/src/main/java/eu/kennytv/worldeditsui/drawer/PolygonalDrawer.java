/*
 * This file is part of WorldEditSUI - https://git.io/wesui
 * Copyright (C) 2018-2021 kennytv (https://github.com/kennytv)
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

package eu.kennytv.worldeditsui.drawer;

import com.sk89q.worldedit.regions.FlatRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;
import eu.kennytv.worldeditsui.WorldEditSUIPlugin;
import eu.kennytv.worldeditsui.compat.Simple2DVector;
import eu.kennytv.worldeditsui.drawer.base.DrawedType;
import eu.kennytv.worldeditsui.drawer.base.DrawerBase;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class PolygonalDrawer extends DrawerBase {

    PolygonalDrawer(final WorldEditSUIPlugin plugin) {
        super(plugin);
    }

    @Override
    public void draw(final Player player, final Region region, final DrawedType drawedType) {
        if (!hasValidSize(player, region)) return;

        final Polygonal2DRegion polyRegion = (Polygonal2DRegion) region;
        final Simple2DVector[] points = plugin.getRegionHelper().getPoints(polyRegion);
        Simple2DVector last = points[0];
        final int bottom = ((FlatRegion) region).getMinimumY();
        final Location location = new Location(player.getWorld(), last.getX(), bottom, last.getZ());
        final int height = region.getHeight();
        final int top = bottom + height;
        final int upwardsTicks = height * settings.getParticlesPerBlock();
        boolean skip = true;
        for (final Simple2DVector point : points) {
            if (skip) {
                skip = false;
                continue;
            }

            connect(point.subtract(last), bottom, top, upwardsTicks, location, player);
            last = point;
            // Just to avoid minor inaccuracy because of double -> int parsing
            location.setX(point.getX());
            location.setZ(point.getZ());
        }
        connect(points[0].subtract(points[points.length - 1]), bottom, top, upwardsTicks, location, player);
    }

    private void connect(final Simple2DVector vector, final int bottom, final int top, final int upwardsTicks, final Location location, final Player player) {
        final double length = vector.length();
        final double factor = length * settings.getParticlesPerBlock();
        final double x = vector.getX() / factor;
        final double z = vector.getZ() / factor;
        final int ticks = (int) factor;

        for (int i = 0; i < ticks; i++) {
            playEffect(location.add(x, 0, z), player);
            location.setY(top);
            playEffect(location, player);
            location.setY(bottom);
        }
        for (int j = 1; j < upwardsTicks; j++) {
            location.setY(location.getY() + settings.getParticleSpace());
            playEffect(location, player);
        }
        location.setY(bottom);
    }
}
