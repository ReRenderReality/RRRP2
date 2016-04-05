package net.re_renderreality.rrrp2.cmd.teleport.admintp;

import java.util.Optional;

import javax.annotation.Nonnull;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import net.re_renderreality.rrrp2.RRRP2;
import net.re_renderreality.rrrp2.backend.CommandExecutorBase;
import net.re_renderreality.rrrp2.database.core.PlayerCore;
import net.re_renderreality.rrrp2.utils.Utilities;

public class TeleportCommand extends CommandExecutorBase {
	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException	{
		Optional<Player> optionalPlayer = ctx.<Player> getOne("player");
		Optional<Player> optionalTarget = ctx.<Player> getOne("target");

		if (optionalPlayer.isPresent())	{
			Player player = optionalPlayer.get();
			int id = RRRP2.getRRRP2().getOnlinePlayer().getIDfromUsername(player.getName());
			PlayerCore playercore = RRRP2.getRRRP2().getOnlinePlayer().getPlayer(id);

			if (optionalTarget.isPresent())	{
				if (src.hasPermission("rrr.admin.tpo.others")) {
					playercore.setLastlocationUpdate(Utilities.convertLocation(player));
					player.setLocation(optionalTarget.get().getLocation());
					src.sendMessage(Text.of(TextColors.GREEN, "Success! ", TextColors.YELLOW, "Teleported player " + player.getName() + " to " + optionalTarget.get().getName()));
					player.sendMessage(Text.of(TextColors.GOLD, "You have been teleported to " + optionalTarget.get().getName() + " by " + src.getName()));
				}
				else {
					src.sendMessage(Text.of(TextColors.DARK_RED, "Error! ", TextColors.RED, "You do not have permission to teleport other players."));
				}
			}
			else {
				if (src instanceof Player) {
					Player targ = (Player) src;
					targ.setLocation(player.getLocation());
					targ.sendMessage(Text.of(TextColors.GREEN, "Success! ", TextColors.YELLOW, "Teleported to player " + player.getName()));
				}
				else {
					src.sendMessage(Text.of(TextColors.DARK_RED, "Error! ", TextColors.RED, "You cannot teleport, you are not a player!"));
				}
			}
		}
		else {
			int x = ctx.<Integer> getOne("x").get();
			int y = ctx.<Integer> getOne("y").get();
			int z = ctx.<Integer> getOne("z").get();
			Optional<String> optionalWorld = ctx.<String> getOne("world");
			Player target = null;

			if (optionalTarget.isPresent() && src.hasPermission("rrr.admin.tpo.others")) {
				target = optionalTarget.get();
			}
			else if (optionalTarget.isPresent()) {
				src.sendMessage(Text.of(TextColors.DARK_RED, "Error! ", TextColors.RED, "You do not have permission to teleport other players."));
				return CommandResult.success();
			}
			else {
				if (src instanceof Player) {
					target = (Player) src;
				}
				else {
					src.sendMessage(Text.of(TextColors.DARK_RED, "Error! ", TextColors.RED, "You cannot teleport, you are not a player!"));
					return CommandResult.success();
				}
			}

			if (!optionalWorld.isPresent()) {
				Location<World> location = new Location<>(target.getWorld(), x, y, z);
				target.setLocation(location);
				target.sendMessage(Text.of(TextColors.GREEN, "Success! ", TextColors.YELLOW, "Teleported to location!"));
			}
			else {
				Optional<World> world = Sponge.getServer().getWorld(optionalWorld.get());

				if (world.isPresent()) {
					Location<World> location = new Location<>(world.get(), x, y, z);

					if (!target.getWorld().getUniqueId().equals(world.get().getUniqueId())) {
						target.transferToWorld(world.get().getUniqueId(), location.getPosition());
					}
					else {
						target.setLocation(location);
					}

					target.sendMessage(Text.of(TextColors.GREEN, "Success! ", TextColors.YELLOW, "Teleported to location!"));
				}
				else {
					target.sendMessage(Text.of(TextColors.DARK_RED, "Error! ", TextColors.RED, "World not found!"));
				}
			}
		}

		return CommandResult.success();
	}

	@Nonnull
	@Override
	public String[] getAliases() {
		return new String[] { "tpo", "TPO" };
	}

	@Nonnull
	@Override
	public CommandSpec getSpec()
	{
		return CommandSpec
			.builder()
			.description(Text.of("Teleport Operator Command"))
			.permission("rrr.admin.tpo")
			.arguments(GenericArguments.firstParsing(
				GenericArguments.seq(
					GenericArguments.onlyOne(GenericArguments.player(Text.of("player"))), 
					GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.player(Text.of("target"))))), 
				GenericArguments.seq(
					GenericArguments.onlyOne(GenericArguments.integer(Text.of("x"))),
					GenericArguments.onlyOne(GenericArguments.integer(Text.of("y"))),
					GenericArguments.onlyOne(GenericArguments.integer(Text.of("z"))), 
					GenericArguments.optional(GenericArguments.string(Text.of("world"))),
					GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.player(Text.of("target")))))))
			.executor(this)
			.build();
	}
}