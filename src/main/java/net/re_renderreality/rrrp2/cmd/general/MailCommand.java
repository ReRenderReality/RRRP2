package net.re_renderreality.rrrp2.cmd.general;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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

import net.re_renderreality.rrrp2.backend.CommandExecutorBase;
import net.re_renderreality.rrrp2.database.Database;
import net.re_renderreality.rrrp2.database.Registry;
import net.re_renderreality.rrrp2.database.core.MailCore;
import net.re_renderreality.rrrp2.database.core.PlayerCore;

public class MailCommand extends CommandExecutorBase {
	private String name;
	private String description;
	private String perm;
	private String useage;
	private String notes;
	
	protected void setLocalVariables() {
		name = "/mail";
		description = "Send an ingame mail to a player";
		perm = "rrr.general.mail";
		useage = "/mail <player> <mail>";
		notes = null;
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
	
	/**
	 * Command to create a MailCore and send it to another player
	 */
	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException	{
		setLocalVariables();
		Optional<String> player = ctx.<String> getOne("player name");
		Optional<Player> pPlayer = ctx.<Player> getOne("player");
		Optional<String> message = ctx.<String> getOne("Message");
		//Database Layout 
		if(src instanceof Player) {
			int targetID;
			PlayerCore target;
			if((player.isPresent() || pPlayer.isPresent()) && message.isPresent()) {
				if(player.isPresent()) {
					targetID = Database.getPlayerIDfromUsername(player.get());
					target = Database.getPlayerCore(targetID);
				} else if (pPlayer.isPresent()) {
					target = Registry.getOnlinePlayers().getPlayerCorefromUsername(pPlayer.get().getName());
					targetID = target.getID();
					if(!(targetID == 0)) {
						pPlayer.get().sendMessage(Text.of("You have received mail!"));
					}	
				} else {
					targetID = 0;
					target = null;
				}
				
				if(!(targetID == 0)) {
					DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					String todaysDate = dateFormat.format(cal.getTime());
					
					Player p = (Player) src;
					PlayerCore cPlayer = Registry.getOnlinePlayers().getPlayerCorefromUsername(p.getName());
					MailCore mail = new MailCore(target.getID(), target.getName(), Database.findNextMailID(), cPlayer.getID(), cPlayer.getName(), todaysDate, message.get(), false);
					mail.insert();
					
					src.sendMessage(Text.of(TextColors.GOLD, "Message successfully sent to: ", TextColors.GRAY, target.getName()));
					return CommandResult.success();
				} else {
					src.sendMessage(Text.of(TextColors.RED, " Name not found in Player database"));
					return CommandResult.empty();
				}
			}
			src.sendMessage(Text.of(TextColors.RED, "Error in Mail Format. Correct Useage: ", TextColors.GRAY, "/mail <player> <message>"));
		} else if(src instanceof ConsoleSource) {
			if(player.isPresent() && message.isPresent()) {
				int targetID = Database.getPlayerIDfromUsername(player.get());
				if(!(targetID == 0)) {
					DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					String todaysDate = dateFormat.format(cal.getTime());
					
					PlayerCore target = Registry.getOnlinePlayers().getPlayer(targetID);
					MailCore mail = new MailCore(target.getID(), target.getName(), Database.findNextID("mail"), 0, "Console", todaysDate, message.get(), false);
					mail.insert();
					
					src.sendMessage(Text.of(TextColors.GOLD, "Message successfully sent to: ", TextColors.GRAY, target.getName()));
					return CommandResult.success();
				} else {
					src.sendMessage(Text.of(TextColors.RED, " Name not found in Player database"));
					return CommandResult.empty();
				}
			} else {
				src.sendMessage(Text.of(TextColors.RED, "Error in Mail Format. Correct Useage: ", TextColors.GRAY, "/mail <player> <message>"));
				return CommandResult.empty();
			}
		} else {
			src.sendMessage(Text.of(TextColors.RED, "If you throw this error you are truely trying this plugin"));
			return CommandResult.empty();
		}
		return CommandResult.empty();
	}

	@Nonnull
	@Override
	public String[] getAliases() {
		return new String[] { "Mail", "mail"};
	}
	
	@Nonnull
	@Override
	public Registry.helpCategory getHelpCategory()
	{
		return Registry.helpCategory.General;
	}
	
	@Nonnull
	@Override
	public CommandSpec getSpec()
	{
		
		return CommandSpec.builder()
			.description(Text.of(description))
			.permission(perm)
			.arguments(GenericArguments.firstParsing(GenericArguments.onlyOne(GenericArguments.player(Text.of("player"))),
						GenericArguments.onlyOne(GenericArguments.string(Text.of("player name")))),
						GenericArguments.remainingJoinedStrings(Text.of("Message")))
			.executor(this)
			.build();
	}
}