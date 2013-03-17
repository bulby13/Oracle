package me.botsko.oracle.commandlibs;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CallInfo {
	
	/**
	 * 
	 */
	private CommandSender sender;
	
	/**
	 * 
	 */
	private Player player;
	
	/**
	 * 
	 */
	private String[] args;

	
	/**
	 * 
	 * @param sender
	 * @param player
	 * @param args
	 */
	public CallInfo(CommandSender sender, Player player, String[] args) {
		this.sender = sender;
		this.player = player;
		this.args = args;
	}

	
	/**
	 * 
	 * @return
	 */
	public Player getPlayer() {
		return player;
	}

	
	/**
	 * 
	 * @return
	 */
	public CommandSender getSender() {
		return sender;
	}
	
	
	/**
	 * 
	 * @param n
	 * @return
	 */
	public String getArg(int n) {
		return this.args[n];
	}
	
	
	/**
	 * 
	 * @return
	 */
	public String[] getArgs() {
	    return this.args;
	}
}
