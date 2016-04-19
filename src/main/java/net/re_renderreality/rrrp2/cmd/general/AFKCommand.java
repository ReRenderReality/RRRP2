package net.re_renderreality.rrrp2.cmd.general;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import net.re_renderreality.rrrp2.RRRP2;
import net.re_renderreality.rrrp2.api.util.config.readers.ReadConfig;
import net.re_renderreality.rrrp2.backend.CommandExecutorBase;
import net.re_renderreality.rrrp2.database.core.PlayerCore;
import net.re_renderreality.rrrp2.utils.AFK;

public class AFKCommand extends CommandExecutorBase
{
	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (src instanceof Player) {
			Logger l = RRRP2.getRRRP2().getLogger();
			Player source = (Player) src;
			PlayerCore player = RRRP2.getRRRP2().getOnlinePlayer().getPlayerCorefromUsername(source.getName());

			if (RRRP2.afkList.containsKey(player.getID()))
			{
				RRRP2.afkList.remove(player.getID());
			}

			int timeBeforeAFK = (int) ReadConfig.getAFKTime();
			long timeToSet = System.currentTimeMillis() - timeBeforeAFK - 1000;
			AFK afk = new AFK(timeToSet);
			RRRP2.afkList.put(player.getID(), afk);
			l.info("AFK SUCCESS!");
		}
		else
		{
			src.sendMessage(Text.of(TextColors.RED, "Must be an in-game player to use /afk!"));
		}

		return CommandResult.success();
	}

	@Nonnull
	@Override
	public String[] getAliases()
	{
		return new String[] { "afk", "AFK" };
	}

	@Nonnull
	@Override
	public CommandSpec getSpec()
	{
		return CommandSpec.builder()
			.description(Text.of("AFK Command"))
			.permission("rrr.general.afk")
			.executor(this)
			.build();
	}
}