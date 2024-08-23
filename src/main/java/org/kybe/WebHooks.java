package org.kybe;

import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.feature.command.Command;
import org.rusherhack.client.api.feature.module.IModule;
import org.rusherhack.client.api.utils.ChatUtils;
import org.rusherhack.core.command.annotations.CommandExecutor;
import org.rusherhack.core.setting.StringSetting;

public class WebHooks extends Command {

	public WebHooks() {
		super("webhooks", "allows to add and remove weebhooks");
	}

	public String getWebHooks() {
		return ((String) ((IModule) RusherHackAPI.getModuleManager().getFeature("DiscordLogger").get()).getSetting("Webhooks").getValue());
	}

	public void setWeebHooks(String hooks) {
		((StringSetting)((IModule) RusherHackAPI.getModuleManager().getFeature("DiscordLogger").get()).getSetting("Webhooks")).setValue(hooks);
	}

	@CommandExecutor(subCommand = "list")
	public void list() {
		String[] weebHooks = getWebHooks().split(",");
		for (int i = 0; i < weebHooks.length; i++) {
			ChatUtils.print(i + ": " + weebHooks[i]);
		}
	}

	@CommandExecutor(subCommand = "add")
	@CommandExecutor.Argument({"weebHook"})
	public void add(String weebHook) {
		if (getWebHooks().isEmpty()) {
			setWeebHooks(weebHook);
			return;
		}
		setWeebHooks(getWebHooks() + "," + weebHook);
		ChatUtils.print("Added weebhook " + weebHook);
	}

	@CommandExecutor(subCommand = "remove")
	@CommandExecutor.Argument({"index"})
	public void remove(int weebHook) {
		String[] weebHooks = getWebHooks().split(",");
		String newWeebHooks = "";
		for (int i = 0; i < weebHooks.length; i++) {
			if (i != weebHook) {
				newWeebHooks += weebHooks[i] + ",";
			}
		}
		setWeebHooks(newWeebHooks);
		ChatUtils.print("Removed weebhook at index " + weebHook);
	}

	@CommandExecutor(subCommand = "clear")
	public void clear() {
		setWeebHooks("");
		ChatUtils.print("Cleared weebhooks");
	}

	@CommandExecutor(subCommand = "dump")
	public void dump() {
		ChatUtils.print(getWebHooks());
	}
}
