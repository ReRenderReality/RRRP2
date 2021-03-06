package net.re_renderreality.rrrp2.cmd.cheats;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.weather.Weather;
import org.spongepowered.api.world.weather.Weathers;

import net.re_renderreality.rrrp2.backend.CommandExecutorBase;
import net.re_renderreality.rrrp2.database.Registry;

public class WeatherCommand extends CommandExecutorBase {
	private String name;
	private String description;
	private String perm;
	private String useage;
	private String notes;
	
	protected void setLocalVariables() {
		name = "/weather";
		description = "Alters the World's weather";
		perm = "rrr.cheat.weather";
		useage = "/weather <clear|rain|storm> (duration)";
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
		Optional<Integer> weatherNum = ctx.<Integer> getOne("weather");
		Optional<Integer> duration = ctx.<Integer> getOne("duration");

		if (src instanceof Player) {
			Player player = (Player) src;
			int numWeather = weatherNum.get();
			Weather weather;
			
			//sets weather of selected world to Weather object
			if (numWeather == 1) {
				weather = Weathers.CLEAR;
				player.sendMessage(Text.of(TextColors.GOLD, "Changing weather to ", TextColors.GRAY, "sunny."));
			} else if (numWeather == 2) {
				weather = Weathers.RAIN;
				player.sendMessage(Text.of(TextColors.GOLD, "Changing weather to ", TextColors.GRAY, "rain."));
			} else if (numWeather == 3) {
				weather = Weathers.THUNDER_STORM;
				player.sendMessage(Text.of(TextColors.GOLD, "Changing weather to ", TextColors.GRAY, "storm."));
			} else {
				src.sendMessage(Text.of(TextColors.DARK_RED, "Error! ", TextColors.RED, "Input invalid! /weather <clear|rain|storm> (duration)"));
				return CommandResult.success();
			}

			if (duration.isPresent()) {
				player.getWorld().setWeather(weather, duration.get());
			}
			else {
				player.getWorld().setWeather(weather);
			}
			return CommandResult.success();
		} else {
			src.sendMessage(Text.of(TextColors.RED, "ERROR! You must be a in-game player to do /weather!"));
			return CommandResult.empty();
		}
	}

	@Nonnull
	@Override
	public String[] getAliases() {
		return new String[] { "weather", "weather" };
	}
	
	@Nonnull
	@Override
	public Registry.helpCategory getHelpCategory()
	{
		return Registry.helpCategory.Cheater;
	}

	@Nonnull
	@Override
	public CommandSpec getSpec() {
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("clear", 1);
		map.put("rain", 2);
		map.put("storm", 3);
		return CommandSpec
				.builder()
				.description(Text.of(description))
				.permission(perm)
				.arguments(GenericArguments.seq(GenericArguments.choices(Text.of("weather"), map)),
							GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.integer(Text.of("duration")))))
				.executor(this)
				.build();
	}
}
