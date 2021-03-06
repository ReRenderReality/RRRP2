package net.re_renderreality.rrrp2.cmd.general;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import net.re_renderreality.rrrp2.backend.CommandExecutorBase;
import net.re_renderreality.rrrp2.database.Registry;

import javax.annotation.Nonnull;

public class ItemDBCommand extends CommandExecutorBase {
	private String name;
	private String description;
	private String perm;
	private String useage;
	private String notes;
	
	protected void setLocalVariables() {
		name = "/itemdb";
		description = "Gives info on the item in your hand";
		perm = "rrr.general.itemdb";
		useage = "/itemdb";
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
	
	//get information about the item being held
	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
		setLocalVariables();
		if (src instanceof Player) {
			Player player = (Player) src;

			if (player.getItemInHand(HandTypes.MAIN_HAND).isPresent()) {
				ItemStack itemInHand = player.getItemInHand(HandTypes.MAIN_HAND).get();
				player.sendMessage(Text.of(TextColors.GOLD, "The ID of the item in your hand is: ", TextColors.GRAY, itemInHand.getItem().getName()));
				player.sendMessage(Text.of(TextColors.GOLD, "The meta of the item in your hand is: ", TextColors.GRAY, itemInHand.toContainer().get(DataQuery.of("UnsafeDamage")).get().toString()));
			} else {
				player.sendMessage(Text.of(TextColors.DARK_RED, "Error! ", TextColors.RED, "You must hold an item."));
			}
		} else {
			src.sendMessage(Text.of(TextColors.DARK_RED, "Error! ", TextColors.RED, "You must be an in-game player to use this command."));
		}

		return CommandResult.success();
	}

	@Nonnull
	@Override
	public String[] getAliases() {
		return new String[] { "iteminfo", "itemdb" };
	}
	
	@Nonnull
	@Override
	public Registry.helpCategory getHelpCategory()
	{
		return Registry.helpCategory.General;
	}

	@Nonnull
	@Override
	public CommandSpec getSpec() {
		return CommandSpec.builder()
				.description(Text.of(description))
				.permission(perm)
				.executor(this)
				.build();
	}
}

