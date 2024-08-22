package org.kybe;

import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.plugin.Plugin;


/**
 * Dc Logger
 *
 * @author kybe236
 */
public class Main extends Plugin {
	
	@Override
	public void onLoad() {
		//creating and registering a new module
		final DiscordLogger discordLogger = new DiscordLogger();
		RusherHackAPI.getModuleManager().registerFeature(discordLogger);

		final WebHooks webhooks = new WebHooks();
		RusherHackAPI.getCommandManager().registerFeature(webhooks);
	}
	
	@Override
	public void onUnload() {
		this.getLogger().info("[] unloaded!");
	}
	
}