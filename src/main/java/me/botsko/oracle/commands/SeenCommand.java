package me.botsko.oracle.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import me.botsko.oracle.Oracle;
import me.botsko.oracle.commandlibs.CallInfo;
import me.botsko.oracle.commandlibs.SubHandler;
import me.botsko.oracle.utils.SeenUtil;

public class SeenCommand implements SubHandler {

	/**
	 * Handle the command
	 */
	public void handle( final CallInfo call ){
		
		String username = null;
		if(call.getArgs().length > 0){
			// Expand partials
			String tmp = Oracle.expandName( call.getArg(0) );
			if(tmp != null){
	    		username = tmp;
	    	}
		} else {
			username = call.getSender().getName();
		}
		
		final OfflinePlayer player = Bukkit.getOfflinePlayer(username);
		
		if( player == null ){
			call.getSender().sendMessage( Oracle.messenger.playerError( "Could not find a player by that name." ) );
			return;
		}
		
		// Check for alt accounts in async thread
    	new Thread(new Runnable(){
			public void run(){
				call.getSender().sendMessage( Oracle.messenger.playerHeaderMsg( player.getName() ) );
				try {
					call.getSender().sendMessage( Oracle.messenger.playerMsg("Joined " + SeenUtil.getPlayerFirstSeen(player)) );
					call.getSender().sendMessage( Oracle.messenger.playerMsg("Last Seen " + SeenUtil.getPlayerLastSeen(player)) );
				} catch (Exception e){
				    call.getSender().sendMessage( Oracle.messenger.playerError( e.getMessage() ) );
				}
			}
    	}).start();
	}
}