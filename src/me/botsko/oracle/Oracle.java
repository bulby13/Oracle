package me.botsko.oracle;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import me.botsko.oracle.commands.PrismOracleCommands;
import me.botsko.oracle.listeners.PrismOraclePlayerListener;
import me.botsko.oracle.utils.JoinUtil;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Oracle extends JavaPlugin {

	/**
	 * Protected/private
	 */
	private String plugin_name;
	private String plugin_version;
	private Logger log = Logger.getLogger("Minecraft");
	private static DataSource pool = new DataSource();
	
	/**
	 * Public
	 */
//	public Language lang;
	public FileConfiguration config;
	public Messenger messenger;

	
    /**
     * Enables the plugin and activates our player listeners
     */
	@Override
	public void onEnable(){
		
		plugin_name = this.getDescription().getName();
		plugin_version = this.getDescription().getVersion();
		
		// Make sure Prism exists
		checkPluginDependancies();
		
		log("Initializing Oracle " + plugin_version + ". By Viveleroi.");
		
		// Loaf config
		loadConfig();
		
//		if(getConfig().getBoolean("prism.notify-newer-versions")){
//			String notice = UpdateNotification.checkForNewerBuild(plugin_version);
//			if(notice != null){
//				log(notice);
//			}
//		}'
		
		// init db
		pool = initDbPool();
		Connection test_conn = dbc();
		if( pool == null || test_conn == null ){
			String[] dbDisabled = new String[3];
			dbDisabled[0] = "Oracle will disable itself because it couldn't connect to a database.";
			dbDisabled[1] = "If you're using MySQL, check your config. Be sure MySQL is running.";
			dbDisabled[2] = "For help - try http://discover-prism.com/wiki/view/troubleshooting/";
			logSection(dbDisabled);
			disablePlugin();
		}
		if(test_conn != null){
			try {
				test_conn.close();
			} catch (SQLException e) {
				logDbError( e );
			}
		}

		if(isEnabled()){
		
//			try {
//			    Metrics metrics = new Metrics(this);
//			    metrics.start();
//			} catch (IOException e) {
//			    log("MCStats submission failed.");
//			}
			
			// Load re-usable libraries
			messenger = new Messenger( plugin_name );
			
			// Add commands
			getCommand("seen").setExecutor( (CommandExecutor) new PrismOracleCommands(this) );
			
			// Register listeners
			getServer().getPluginManager().registerEvents(new PrismOraclePlayerListener(this), this);
			
			// Register tasks
			catchUncaughtDisconnects();
			
		}
	}
	
	
	/**
	 * Load configuration and language files
	 */
	public void loadConfig(){
		OracleConfig mc = new OracleConfig( this );
		config = mc.getConfig();
		// Load language files
//		lang = new Language( mc.getLang( config.getString("oracle.language") ) );
	}
	
	
	/**
	 * 
	 */
	public void checkPluginDependancies(){
		
//		// Prism 
//		Plugin _tempPrism = getServer().getPluginManager().getPlugin("Prism");
//		if (_tempPrism != null) {
//			prism = (Prism)_tempPrism;
//			prism.log("Prism Core (anti-grief) found!");
//		}
//		else {
//			prism.log("Prism Core (anti-grief) not found. Plugin add-on may not run.");
//			this.disablePlugin();
//		}
	}
	
	
	/**
	 * 
	 * @return
	 */
	public DataSource initDbPool(){
		
		DataSource pool = null;
		String dns = "jdbc:mysql://"+config.getString("prism.mysql.hostname")+":"+config.getString("prism.mysql.port")+"/"+config.getString("prism.mysql.database");
		pool = new DataSource();
		pool.setDriverClassName("com.mysql.jdbc.Driver");
		pool.setUrl(dns);
	    pool.setUsername(config.getString("prism.mysql.username"));
	    pool.setPassword(config.getString("prism.mysql.password"));
		pool.setMaxActive( config.getInt("prism.database.max-pool-connections") );
		pool.setMaxIdle( config.getInt("prism.database.max-pool-connections") );
	    pool.setMaxWait( config.getInt("prism.database.max-wait") );
	
		return pool;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public static DataSource getPool(){
		return pool;
	}
	
	
	/**
	 * 
	 * @return
	 * @throws SQLException 
	 */
	public static Connection dbc(){
		Connection con = null;
		try {
			con = pool.getConnection();
		} catch (SQLException e) {
			System.out.print("Database connection failed. " + e.getMessage());
			e.printStackTrace();
		}
		return con;
	}
	
	
	/**
	 * If a user disconnects in an unknown way that is never caught by onPlayerQuit,
	 * this will force close all records except for players currently online.
	 */
	public void catchUncaughtDisconnects(){
		getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable(){
		    public void run(){
		    	String on_users = "";
				for(Player pl: getServer().getOnlinePlayers()) {
					on_users += "'"+pl.getName()+"',";
				}
				if(!on_users.isEmpty()){
					on_users = on_users.substring(0, on_users.length()-1);
				}
				JoinUtil.forceDateForOfflinePlayers( on_users );
		    }
		}, 1200L, 1200L);
	}
	
	
	/**
     * Partial username matching
     * @param Name
     * @return
     */
    public String expandName(String Name) {
        int m = 0;
        String Result = "";
        for (int n = 0; n < getServer().getOnlinePlayers().length; n++) {
            String str = getServer().getOnlinePlayers()[n].getName();
            if (str.matches("(?i).*" + Name + ".*")) {
                m++;
                Result = str;
                if(m==2) {
                    return null;
                }
            }
            if (str.equalsIgnoreCase(Name))
                return str;
        }
        if (m == 1)
            return Result;
        if (m > 1) {
            return Name;
        }
        if (m < 1) {
            return Name;
        }
        return Name;
    }
    
    
    /**
	 * 
	 * @param message
	 */
	public void log(String message){
		log.info("["+plugin_name+"]: " + message);
	}
	
    
    /**
	 * 
	 * @param message
	 */
	public void logSection(String[] messages){
		if(messages.length > 0){
			log("--------------------- ## Important ## ---------------------");
			for(String msg : messages){
				log(msg);
			}
			log("--------------------- ## ========= ## ---------------------");
		}
	}
	
	
	/**
	 * 
	 */
	public void logDbError( SQLException e ){
		log("Database connection error: " + e.getMessage());
		if(e.getMessage().contains("marked as crashed")){
			String[] msg = new String[2];
			msg[0] = "If MySQL crashes during write it may corrupt it's indexes.";
			msg[1] = "Try running `CHECK TABLE prism_actions` and then `REPAIR TABLE prism_actions`.";
			logSection(msg);
		}
		e.printStackTrace();
	}
	
	
	/**
	 * Disable the plugin
	 */
	public void disablePlugin(){
		this.setEnabled(false);
	}
	
	
	/**
	 * Shutdown
	 */
	@Override
	public void onDisable(){
		log("Closing plugin.");
	}
}