package net.re_renderreality.rrrp2.cmd.cheats;

import java.util.Optional;

import javax.annotation.Nonnull;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.source.CommandBlockSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.manipulator.mutable.item.EnchantmentData;
import org.spongepowered.api.data.meta.ItemEnchantment;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.Enchantment;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import net.re_renderreality.rrrp2.api.util.config.readers.ReadConfig;
import net.re_renderreality.rrrp2.backend.CommandExecutorBase;
import net.re_renderreality.rrrp2.database.Registry;

public class EnchantCommand extends CommandExecutorBase {
	private String name;
	private String description;
	private String perm;
	private String useage;
	private String notes;
	
	protected void setLocalVariables() {
		name = "/enchant";
		description = "Enchants the item in your hand";
		perm = "rrr.cheat.enchant";
		useage = "/enchant (target) <level> <enchantment>";
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
	
	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
		setLocalVariables();
		Optional<Player> target = ctx.<Player> getOne("target");
		String enchantmentName = ctx.<String> getOne("enchantment").get();
		int level = ctx.<Integer> getOne("level").get();

		Enchantment enchantment = Sponge.getRegistry().getType(Enchantment.class, enchantmentName).orElse(null);
		
		if (enchantment == null) {
			src.sendMessage(Text.of(TextColors.DARK_RED, "Error! ", TextColors.RED, "Enchantment specified not found!"));
			return CommandResult.success();
		}

		//checks if unsafe enchantments are enabled
		if (!ReadConfig.getUnsafeEnchantmentStatus()) {
			
			if (enchantment.getMaximumLevel() < level) {
				src.sendMessage(Text.of(TextColors.DARK_RED, "Error! ", TextColors.RED, "Enchantment level too high!"));
				return CommandResult.success();
			}
		}

		if (!target.isPresent()) {
			if (src instanceof Player) {
				Player player = (Player) src;

				if (player.getItemInHand(HandTypes.MAIN_HAND).isPresent())	{
					ItemStack itemInHand = player.getItemInHand(HandTypes.MAIN_HAND).get();

					//checks if enchantment can be applied
					if (!enchantment.canBeAppliedToStack(itemInHand)) {
						src.sendMessage(Text.of(TextColors.DARK_RED, "Error! ", TextColors.RED, "Enchantment cannot be applied to this item!"));
						return CommandResult.empty();
					}

					EnchantmentData enchantmentData = itemInHand.getOrCreate(EnchantmentData.class).get();
					ItemEnchantment itemEnchantment = new ItemEnchantment(enchantment, level);
					ItemEnchantment sameEnchantment = null;

					for (ItemEnchantment ench : enchantmentData.enchantments()) {
						if (ench.getEnchantment().getId().equals(enchantment.getId())) {
							sameEnchantment = ench;
							break;
						}
					}

					if (sameEnchantment == null) {
						enchantmentData.set(enchantmentData.enchantments().add(itemEnchantment));
					}
					else {
						enchantmentData.set(enchantmentData.enchantments().remove(sameEnchantment));
						enchantmentData.set(enchantmentData.enchantments().add(itemEnchantment));
					}
					
					//applies enchant
					itemInHand.offer(enchantmentData);
					player.setItemInHand(HandTypes.MAIN_HAND, itemInHand);
					player.sendMessage(Text.of(TextColors.GREEN, "Success! ", TextColors.YELLOW, "Enchanted item(s) in your hand."));
				}
				else {
					src.sendMessage(Text.of(TextColors.DARK_RED, "Error! ", TextColors.RED, "You must be holding something to enchant!"));
				}
			}
			else if (src instanceof ConsoleSource) {
				src.sendMessage(Text.of(TextColors.DARK_RED, "Error! ", TextColors.RED, "Must be an in-game player to use /enchant!"));
			}
			else if (src instanceof CommandBlockSource) {
				src.sendMessage(Text.of(TextColors.DARK_RED, "Error! ", TextColors.RED, "Must be an in-game player to use /enchant!"));
			}
		//same as above but can chant another person's held items
		} else if (target.isPresent() && src.hasPermission("rrr.cheat.enchant.others")) {
			Player player = target.get();

			if (player.getItemInHand(HandTypes.MAIN_HAND).isPresent()) {
				ItemStack itemInHand = player.getItemInHand(HandTypes.MAIN_HAND).get();

				if (!enchantment.canBeAppliedToStack(itemInHand)) {
					src.sendMessage(Text.of(TextColors.DARK_RED, "Error! ", TextColors.RED, "Enchantment cannot be applied to this item!"));
					return CommandResult.success();
				}

				EnchantmentData enchantmentData = itemInHand.getOrCreate(EnchantmentData.class).get();
				ItemEnchantment itemEnchantment = new ItemEnchantment(enchantment, level);
				ItemEnchantment sameEnchantment = null;

				for (ItemEnchantment ench : enchantmentData.enchantments()) {
					if (ench.getEnchantment().getId().equals(enchantment.getId())) {
						sameEnchantment = ench;
						break;
					}
				}

				if (sameEnchantment == null) {
					enchantmentData.set(enchantmentData.enchantments().add(itemEnchantment));
				}
				else {
					enchantmentData.set(enchantmentData.enchantments().remove(sameEnchantment));
					enchantmentData.set(enchantmentData.enchantments().add(itemEnchantment));
				}

				itemInHand.offer(enchantmentData);
				player.setItemInHand(HandTypes.MAIN_HAND, itemInHand);
				player.sendMessage(Text.of(TextColors.GREEN, "Success! ", TextColors.YELLOW, "Enchanted item(s) in your hand."));
			}
			else {
				src.sendMessage(Text.of(TextColors.DARK_RED, "Error! ", TextColors.RED, "You must be holding something to enchant!"));
			}
		}

		return CommandResult.success();
	}

	@Nonnull
	@Override
	public String[] getAliases()
	{
		return new String[] { "enchant", "ench", "Enchant" };
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
			.arguments(GenericArguments.seq(GenericArguments.optional(GenericArguments.player(Text.of("target"))), 
											GenericArguments.onlyOne(GenericArguments.integer(Text.of("level")))), 
											GenericArguments.onlyOne(GenericArguments.remainingJoinedStrings(Text.of("enchantment"))))
			.executor(this)
			.build();
	}
}
