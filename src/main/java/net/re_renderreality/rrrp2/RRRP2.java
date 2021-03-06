package net.re_renderreality.rrrp2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import net.re_renderreality.rrrp2.backend.CommandLoader;
import net.re_renderreality.rrrp2.config.*;
import net.re_renderreality.rrrp2.database.Database;
import net.re_renderreality.rrrp2.database.OnlinePlayers;
import net.re_renderreality.rrrp2.database.Registry;
import net.re_renderreality.rrrp2.listeners.*;
import net.re_renderreality.rrrp2.utils.AFK;
import net.re_renderreality.rrrp2.utils.HelpGenerator;
import net.re_renderreality.rrrp2.utils.Log;
import net.re_renderreality.rrrp2.utils.SurroundedPlayer;
import net.re_renderreality.rrrp2.utils.TPInvitation;
import net.re_renderreality.rrrp2.utils.Utilities;
import net.re_renderreality.rrrp2.utils.WorldHandler;

import org.slf4j.Logger;
//import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

import me.flibio.updatifier.Updatifier;

@Updatifier(repoName = "RRRP2", repoOwner = "ReRenderReality", version = "v" + PluginInfo.VERSION)
@Plugin(id = "rrr.commands", name = "RRRP2", version = "0.1.0-ALPHA", description = "basic commands for RRP2 Bundle") 
public class RRRP2{
	
	protected RRRP2 () {
		
	}
	
	@Inject 
	public Game game;
	@Inject 
	private PluginContainer container;
	@Inject 
	private Logger logger;
	@Inject
	@ConfigDir(sharedRoot = false)
	private Path configDir;
	
	public enum DebugLevel { ALL, DEBUG, CONFIG, INFO, OFF, SEVERE, WARNING }
	
	public static RRRP2 plugin;
	public static HashMap<Integer, SurroundedPlayer> surrounded = new HashMap<Integer, SurroundedPlayer>();
	public static HashMap<Integer, AFK> afkList = new HashMap<>();
	public static HashMap<String, CommandSource> recentlyMessaged = new HashMap<>();
	public static List<Player> recentlyJoined = Lists.newArrayList();
	public static List<Player> onePunch = Lists.newArrayList();
	public static List<Player> jockey = Lists.newArrayList();
	public static List<Player> socialSpy = Lists.newArrayList();
	public static List<Player> invisiblePlayers = Lists.newArrayList();
	public static Set<Integer> teleportingPlayers = Sets.newHashSet();
	public static List<TPInvitation> pendingInvites = Lists.newArrayList();
	public static Set<Integer> frozenPlayers = Sets.newHashSet();
	private Server server;
	private OnlinePlayers onlinePlayer = new OnlinePlayers();
	
	public static RRRP2 getRRRP2() {
		return plugin;
	}
	/**
	 * @param event Listener for GameStartingServerEvent.
	 * @note Initialization of configs.
	 */
	@Listener
	public void onPreInitialization(GamePreInitializationEvent event)
	{
		Log.setLogger(logger);
		logger.info(container.getName() + ": Config Initiallation Beginning....");
		plugin = this;

		// Create Config Directory for EssentialCmds
		if (!Files.exists(configDir))
		{
			logger.info(container.getName() + ": Config root not found generating...");
			if (Files.exists(configDir.resolveSibling("RRRP2")))
			{
				try
				{
					Files.move(configDir.resolveSibling("RRRP2"), configDir);
					logger.info(container.getName() + ": Config root generated");
				}
				catch (IOException e) 
				{
					e.printStackTrace();
				}
			}
			else
			{
				try
				{
					Files.createDirectories(configDir);
					logger.info(container.getName() + ": Config root generated");
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			
		}

		// Create data Directory for the Plugin
		if (!Files.exists(configDir.resolve("data")))
		{
			logger.info(container.getName() + ": Config data subfolder not found generating...");
			try
			{
				logger.info(container.getName() + ": Config data subfolder generated");
				Files.createDirectories(configDir.resolve("data"));
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		Config.getConfig().setup();
		Messages.getConfig().setup();
		MOTD.getConfig().setup();
		Rules.getConfig().setup();
		Announcements.getConfig().setup();		
		
		Log.info(container.getName() + ": Config Initiallation Finished");
	}
	
	/**
	 * @param event Listener for GameInitializationEvent.
	 * @note Initialization of Listeners, Plugin, registry.
	 */
	@Listener
	public void onServerInit(GameInitializationEvent event)
	{
		Utilities.startAFKService();
		
		server = game.getServer();
		Registry.setServer(server);
		Registry.setGame(getGame());
		Registry.setOnlinePlayers(getOnlinePlayer());
		CommandLoader.registerCommands();
		
		HelpGenerator.getHelp().populate();
		
		getGame().getEventManager().registerListeners(this, new PlayerJoinListener());
		getGame().getEventManager().registerListeners(this, new MessageListener());
		getGame().getEventManager().registerListeners(this, new PlayerLeftEvent());
		getGame().getEventManager().registerListeners(this, new DamageListener());
		getGame().getEventManager().registerListeners(this, new DeathListener());
		getGame().getEventManager().registerListeners(this, new CommandListener());
		getGame().getEventManager().registerListeners(this, new ChatListener());
		getGame().getEventManager().registerListeners(this, new TPListener());
		getGame().getEventManager().registerListeners(this, new PlayerMoveListener());
		getGame().getEventManager().registerListeners(this, new PlayerDisplacementListener());
		getGame().getEventManager().registerListeners(this, new LeftClickEntityListener());
		getGame().getEventManager().registerListeners(this, new RightClickEntityListener());
		getGame().getEventManager().registerListeners(this, new WeatherListener());
		getGame().getEventManager().registerListeners(this, new BlockBreakListener());
	}
	
	/**
	 * @param event Listener for GameStartingServerEvent.
	 * @note Initialization of  Database.
	 */
	@Listener 
	public void gameStarting(GameStartingServerEvent event) {
	
		Database.setup(game);
    	Database.load(game);
    	WorldHandler.setup();
		
		Log.info(container.getName() + " v" + container.getVersion().get() + " has successfully been initialized.");
	}
	
	/**
	 * @param event Listener for "GameStoppingServerEvent" event.
	 * @note will push everything to database emergency commit
	 */
	@Listener 
	public void gameStopping(GameStoppingServerEvent event) {
		
		Log.info(container.getName() + " v" + container.getVersion() + " has successfully been un-initialized.");
	}
	
	/**
	 * @return returns the directory of the configuration files
	 */
	public Path getConfigDir() {
		return configDir;
	}

	/**
	 * @return current Server Object.
	 */
	public Server getServer() { return server; }
	
	/**
	 * @return current Game Object.
	 */
	public Game getGame() { return game; }
	
	/**
	 * @return current Online Player object
	 */
	private OnlinePlayers getOnlinePlayer() { return onlinePlayer;	}
}