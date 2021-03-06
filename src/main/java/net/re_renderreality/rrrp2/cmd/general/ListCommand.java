package net.re_renderreality.rrrp2.cmd.general;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.serializer.TextSerializers;

import com.google.common.collect.Lists;

import net.re_renderreality.rrrp2.RRRP2;
import net.re_renderreality.rrrp2.backend.CommandExecutorBase;
import net.re_renderreality.rrrp2.database.Registry;
import net.re_renderreality.rrrp2.database.core.PlayerCore;
import net.re_renderreality.rrrp2.utils.AFK;
import net.re_renderreality.rrrp2.utils.SubjectComparator;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;

public class ListCommand extends CommandExecutorBase {
	private String name;
	private String description;
	private String perm;
	private String useage;
	private String notes;
	
	protected void setLocalVariables() {
		name = "/list";
		description = "Displays a list of all online players";
		perm = "rrr.general.list";
		useage = "/list";
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
		//Checks for plugins like PEX
		Optional<PermissionService> optPermissionService = Sponge.getServiceManager().provide(PermissionService.class);

		src.sendMessage(Text.of(TextColors.GOLD, "There are ", TextColors.RED, Sponge.getServer().getOnlinePlayers().size(), TextColors.GOLD, " out of ", TextColors.GRAY, Sponge.getServer().getMaxPlayers(), TextColors.GOLD, " players online."));

		if (optPermissionService.isPresent()) {
			PermissionService permissionService = optPermissionService.get();
			List<Subject> groups = Lists.newArrayList(permissionService.getGroupSubjects().getAllSubjects());
			Collections.sort(groups, new SubjectComparator());
			Collections.reverse(groups);
			List<UUID> listedPlayers = Lists.newArrayList();
			
			//loop through every group
			for (Subject group : groups) {
				List<Subject> users = StreamSupport.stream(permissionService.getUserSubjects().getAllSubjects().spliterator(), false).filter(u -> u.isChildOf(group)).collect(Collectors.toList());
				List<Text> onlineUsers = Lists.newArrayList();

				for (Subject user : users) {
					
					Optional<Player> optPlayer;

					try	{
						optPlayer = Sponge.getServer().getPlayer(UUID.fromString(user.getIdentifier()));
					} catch (IllegalArgumentException e) {
						optPlayer = Sponge.getServer().getPlayer(user.getIdentifier());
					}

					//Add Player to player list
					if (optPlayer.isPresent() && !listedPlayers.contains(optPlayer.get().getUniqueId())) {
						Player player = optPlayer.get();
						PlayerCore playa = Registry.getOnlinePlayers().getPlayerCorefromUsername(player.getName());
						listedPlayers.add(optPlayer.get().getUniqueId());
						Text name = null;
						if(!(playa.getNick().equals(""))) {
							name = Text.builder().append(TextSerializers.FORMATTING_CODE.deserialize(playa.getNick())).append(Text.of(TextColors.GOLD, " | ")).build();
						} else {
							name = Text.of(TextColors.GRAY, playa.getName());
						}
						boolean isAFK = false;

						if (RRRP2.afkList.containsKey(playa.getID())) {
							AFK afk = RRRP2.afkList.get(playa.getID());
							isAFK = afk.getAFK();
						}

						if (isAFK)
							onlineUsers.add(Text.builder().append(Text.of(TextColors.GRAY, TextStyles.ITALIC, "[AFK]: ")).append(name).build());
						else
							onlineUsers.add(name);
					}
				}

				if (onlineUsers.size() > 0) {
					src.sendMessage(Text.builder().append(Text.of(TextColors.GOLD, group.getIdentifier(), ": ")).append(onlineUsers).build());
				}
			}

			if (listedPlayers.size() < Sponge.getServer().getOnlinePlayers().size()) {
				List<Player> remainingPlayers = Sponge.getServer().getOnlinePlayers().stream().filter(p -> !listedPlayers.contains(p.getUniqueId())).collect(Collectors.toList());
				List<Text> onlineUsers = Lists.newArrayList();

				for (Player player : remainingPlayers) {
					PlayerCore playa = Registry.getOnlinePlayers().getPlayerCorefromUsername(player.getName());
					Text name = Text.builder().append(TextSerializers.FORMATTING_CODE.deserialize(playa.getNick())).append(Text.of(" ")).build();
					boolean isAFK = false;

					if (RRRP2.afkList.containsKey(player.getUniqueId())) {
						AFK afk = RRRP2.afkList.get(player.getUniqueId());
						isAFK = afk.getAFK();
					}

					if (isAFK)
						onlineUsers.add(Text.builder().append(Text.of(TextColors.GRAY, TextStyles.ITALIC, "[AFK]: ")).append(name).build());
					else
						onlineUsers.add(name);
				}

				if (onlineUsers.size() > 0)	{
					src.sendMessage(Text.builder().append(Text.of(TextColors.GOLD, "Default", ": ")).append(onlineUsers).build());
				}
			}
		} //end PEX
		else {
			src.sendMessage(Text.of(TextColors.GRAY, Sponge.getServer().getOnlinePlayers().toString()));
		}

		return CommandResult.success();
	}

	@Nonnull
	@Override
	public String[] getAliases()
	{
		return new String[] { "list", "List" };
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
			.executor(this)
			.build();
	}
}
