/*
 * This file is part of EssentialCmds, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2015 - 2015 HassanS6000
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.re_renderreality.rrrp2.cmd.cheats;

import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.Extent;

import net.re_renderreality.rrrp2.backend.CommandExecutorBase;
import net.re_renderreality.rrrp2.database.Registry;

import java.util.Optional;

import javax.annotation.Nonnull;

//TODO: Update the command failed message
public class SmiteCommand extends CommandExecutorBase {
	private String name;
	private String description;
	private String perm;
	private String useage;
	private String notes;
	
	protected void setLocalVariables() {
		name = "/smite";
		description = "Smite someone with the power of Thor";
		perm = "rrr.cheat.smite";
		useage = "/smite (target)";
		notes = "If no target strikes where you look";
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public String getPerm() {
		return this.perm;
	}
	
	public String getUseage() {
		return this.useage;
	}
	
	public String getNotes() {
		return this.notes;
	}
	
	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
		setLocalVariables();
		Optional<Player> optionalTarget = ctx.<Player> getOne("player");

		//creates a lightning entity at the location looked at or at a player
		if (!optionalTarget.isPresent()) {
			
			if (src instanceof Player || src instanceof ConsoleSource) {
				if(src instanceof Player) {
					Player player = (Player) src;
					
				    //block ray hits block
					BlockRay<World> playerBlockRay = BlockRay.from(player).distanceLimit(350).build();
					BlockRayHit<World> finalHitRay = null;

					while (playerBlockRay.hasNext()) {
						BlockRayHit<World> currentHitRay = playerBlockRay.next();

						if (!player.getWorld().getBlockType(currentHitRay.getBlockPosition()).equals(BlockTypes.AIR)) {
							finalHitRay = currentHitRay;
							break;
						}
					}

					Location<World> lightningLocation = null;

					//if ray hit nothing then use player location
					if (finalHitRay == null) {
						lightningLocation = player.getLocation();
					}
					else {
						lightningLocation = finalHitRay.getLocation();
					}

					spawnEntity(lightningLocation, src);
					player.sendMessage(Text.of(TextColors.GREEN, "Success! ", TextColors.GOLD, "You Smited the Ground"));
				}
			}
			else {
				src.sendMessage(Text.of(TextColors.RED, "Error! Must be an in-game player to use /lightning!"));
			}
		} else {
			Player player = optionalTarget.get();
			Location<World> playerLocation = player.getLocation();
			spawnEntity(playerLocation, src);
			player.sendMessage(Text.of(TextColors.GRAY, src.getName(), TextColors.GOLD, " has struck you with lightning."));
			src.sendMessage(Text.of(TextColors.GREEN, "Success! ", TextColors.YELLOW, "Struck " + player.getName() + " with lightning."));
		}

		return CommandResult.success();
	}

	/**
	 * spawns lightning entity at location
	 * @param location to spawn at
	 * @param src who executed command
	 */
	private void spawnEntity(Location<World> location, CommandSource src)
	{
		Extent extent = location.getExtent();
		Entity lightning = extent.createEntity(EntityTypes.LIGHTNING, location.getPosition());
		extent.spawnEntity(lightning, Cause.of(NamedCause.source(SpawnCause.builder().type(SpawnTypes.PLUGIN).build())));
	}

	@Nonnull
	@Override
	public String[] getAliases()
	{
		return new String[] { "Zeus", "Smite", "Lightning", "smite", "Lightning" };
	}
	
	@Nonnull
	@Override
	public Registry.helpCategory getHelpCategory()
	{
		return Registry.helpCategory.Cheater;
	}

	@Nonnull
	@Override
	public CommandSpec getSpec()
	{
		return CommandSpec
				.builder()
				.description(Text.of(description))
				.permission(perm)
				.arguments(GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.player(Text.of("player")))))
				.executor(this).build();
	}
}
