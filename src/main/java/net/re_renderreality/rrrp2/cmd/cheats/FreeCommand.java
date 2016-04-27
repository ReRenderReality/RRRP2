package net.re_renderreality.rrrp2.cmd.cheats;

import java.util.Optional;

import javax.annotation.Nonnull;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import net.re_renderreality.rrrp2.RRRP2;
import net.re_renderreality.rrrp2.backend.CommandExecutorBase;
import net.re_renderreality.rrrp2.database.Database;
import net.re_renderreality.rrrp2.database.Registry;
import net.re_renderreality.rrrp2.utils.SurroundedPlayer;

public class FreeCommand extends CommandExecutorBase {

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		Optional<Player> player = ctx.<Player> getOne("Player");
		
		if(src instanceof Player || src instanceof ConsoleSource) {
			if(player.isPresent()) {
				Player target = player.get();
				Registry.getOnlinePlayers().getIDfromUsername(target.getName());
				int id = Database.getID(target.getUniqueId().toString());
				
				//frees the player and replaces the old blocks
				if(RRRP2.surrounded.containsKey(id)) {
					SurroundedPlayer p = RRRP2.surrounded.get(id);
					p.free();
					RRRP2.surrounded.remove(id);
					target.sendMessage(Text.of(TextColors.GOLD, "You have been Freed!"));
					src.sendMessage(Text.of(TextColors.GREEN, "Success! ", TextColors.GOLD, " You have freed ", TextColors.GRAY, target.getName()));
					CommandResult.success();
				}
		        src.sendMessage(Text.of(TextColors.RED, "Error! Command Format is: /free <Player>"));
		        return CommandResult.empty();
			}
		}
		src.sendMessage(Text.of(TextColors.RED, "Only Players and the Console can execute this command!"));
		return CommandResult.empty();
	}
	
	@Nonnull
	@Override
	public String[] getAliases() {
		return new String[] { "free", "Free"};
	}
	
	@Nonnull
	@Override
	public CommandSpec getSpec() {
		return CommandSpec.builder()
				.description(Text.of("Frees a surrounded player"))
				.permission("rrr.cheat.free")
				.arguments(GenericArguments.onlyOne(GenericArguments.player(Text.of("Player"))))
				.executor(this).build();
	}
}
