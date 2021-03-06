package net.re_renderreality.rrrp2.listeners;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import net.re_renderreality.rrrp2.RRRP2;
import net.re_renderreality.rrrp2.api.util.config.readers.ReadConfigMesseges;
import net.re_renderreality.rrrp2.database.Database;
import net.re_renderreality.rrrp2.database.core.PlayerCore;
import net.re_renderreality.rrrp2.utils.AFK;
import net.re_renderreality.rrrp2.utils.Utilities;
import net.re_renderreality.rrrp2.database.OnlinePlayers;
import net.re_renderreality.rrrp2.database.Registry;


public class PlayerJoinListener
{
	/**
	 * @param event client connection event
	 * 
	 * TODO: Show Mail Notification, check Username for changes,
	 */
	@Listener
	public void onPlayerJoin(ClientConnectionEvent.Join event) {
		Player player = event.getTargetEntity();
		
		String uuid = player.getUniqueId().toString();
		int id = Database.getIDFromDatabase(uuid);
		
		boolean firstjoin = false;
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		String todaysDate = dateFormat.format(cal.getTime());
		if ( id == 0) {
			id = Database.findNextID("players");
			
			firstjoin = true;
			PlayerCore thePlayer = new PlayerCore(id,player.getUniqueId().toString(),player.getName(), player.getConnection().getAddress().getHostString(), "", "default", 5.0, false, false, false, false, false, false, false, 0.0, null, null, null, null );
			Database.addUUID(uuid, id);
			thePlayer.insert();

			if (ReadConfigMesseges.getFirstJoinMsgEnabled()); {
				String connectionMessage = ReadConfigMesseges.getFirstJoinMsg();
				connectionMessage = connectionMessage.replaceAll("%player", player.getName());
				Text newMessage = TextSerializers.formattingCode('&').deserialize(connectionMessage);
				event.setMessage(newMessage);
				
			}
			if (ReadConfigMesseges.getUniqueMsgShow()) {
				String uniquePlayerCount = ReadConfigMesseges.getUniqueMsg();
				uniquePlayerCount = uniquePlayerCount.replaceAll("%players", String.valueOf(id));
				Text newMessage = TextSerializers.formattingCode('&').deserialize(uniquePlayerCount);
				Utilities.broadcastMessage(newMessage);
			}
		} else {
			String connectionMessage = ReadConfigMesseges.getJoinMsg();
			Database.addUUID(uuid, id);
			if (ReadConfigMesseges.getJoinMsgEnabled())
			{
				connectionMessage = connectionMessage.replaceAll("%player", player.getName());
				Text newMessage = TextSerializers.formattingCode('&').deserialize(connectionMessage);
				event.setMessage(newMessage);
			}
		}
		
		RRRP2.recentlyJoined.add(event.getTargetEntity());
		
		OnlinePlayers OP = Registry.getOnlinePlayers();
		PlayerCore players = Database.getPlayerCore(id);
		
		if(players.getInvisible()) {
			if(player.get(Keys.INVISIBLE).isPresent()) {
				player.offer(Keys.INVISIBLE, true);
				player.sendMessage(Text.of(TextColors.GOLD, "Toggled Visibillity: ", TextColors.GRAY, "off"));
				RRRP2.invisiblePlayers.add(player);
				players.setInvisibleUpdate(!players.getInvisible());
			}
		}
		
		if (firstjoin) {
			players.setFirstseenUpdate(todaysDate);
		}
		
		players.setIPUpdate(player.getConnection().getAddress().getHostString());
		players.setLastseenUpdate(todaysDate);
		Database.commit();
		OP.addPlayer(players);
		
		if (RRRP2.afkList.containsKey(players.getID())) {
			RRRP2.afkList.remove(players.getID());
		}
		
		
		RRRP2.afkList.put(players.getID(), new AFK(System.currentTimeMillis()));
		
		//if tempban runs out player is still in the mySQL banned list
		if(players.getBanned()) {
			Database.execute("DELETE FROM bans WHERE ID = " + id + ";");
			players.setBannedUpdate(false);
		}
	}
}
