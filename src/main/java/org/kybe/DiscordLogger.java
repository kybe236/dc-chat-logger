package org.kybe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import org.rusherhack.client.api.events.network.EventPacket;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.client.api.setting.ColorSetting;
import org.rusherhack.client.api.utils.ChatUtils;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.setting.BooleanSetting;
import org.rusherhack.core.setting.EnumSetting;
import org.rusherhack.core.setting.StringSetting;

import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;


/**
 * Dc Logger
 *
 * @author kybe236
 */
public class DiscordLogger extends ToggleableModule {
	/**
	 * Settings
	 */
	public enum OptionEnum {
		All,
		Specific,
	}

	private final EnumSetting<OptionEnum> option = new EnumSetting<>("Mode", "Wich mode to use", OptionEnum.All);
	private final StringSetting players = new StringSetting("Players", "Wich players should be watched , seperated", "");
	private final ColorSetting color = new ColorSetting("Webhook Color", "Webhook color", new Color(255, 0, 0, 255));
	private final BooleanSetting ignoreSelf = new BooleanSetting("Ignore Self", "Ignore your own messages", true);
	private final BooleanSetting avatar = new BooleanSetting("Avatar", "Show avatar", true);
	private final StringSetting weebHook = new StringSetting("Webhooks", "Weebhook displayed with *", "") {
		@Override
		public String getDisplayValue() {
			return ((this.getValue().isEmpty()) ? "0" : String.valueOf(this.getValue().split(",").length));
		}
	};

	/**
	 * Enum for the avatar setting
	 */
	public enum AvatarEnum {
		HEAD,
		BODY,
		PLAYER,
		COMBO,
		SKIN
	}
	public enum BodyEnum {
		LEFT,
		RIGHT
	}

	/**
	 * SubSettings for the avatar setting
	 */
	public final StringSetting avatarSize = new StringSetting("Size", "Size of the avatar", "64");
	public final BooleanSetting nohelm = new BooleanSetting("No Helm", "Show the avatar without the helmet", false);
	public final EnumSetting<AvatarEnum> avatarType = new EnumSetting<>("Type", "Type of the avatar", AvatarEnum.HEAD)
			.onChange(newv -> {
				if (newv == AvatarEnum.BODY) {
					this.bodylr.setVisibility(new BooleanSupplier() {
						@Override
						public boolean getAsBoolean() {
							return true;
						}
					});
				} else {
					this.bodylr.setVisibility(new BooleanSupplier() {
						@Override
						public boolean getAsBoolean() {
							return false;
						}
					});
				}
			});
	public final EnumSetting<BodyEnum> bodylr = new EnumSetting<>("Orientation", "Where the player is facing", BodyEnum.LEFT);

	/**
	 * Constructor
	 */
	public DiscordLogger() {
		super("DiscordLogger", "logs chat messages to discord", ModuleCategory.CLIENT);

		this.avatar.addSubSettings(
				this.avatarSize,
				this.nohelm,
				this.avatarType,
				this.bodylr
		);

		//register settings
		this.registerSettings(
				this.option,
				this.players,
				this.color,
				this.ignoreSelf,
				this.avatar,
				this.weebHook
		);
	}

	HashMap<UUID, String> playerCache = new HashMap<>();

	private boolean pauseListener = false;

	@Subscribe
	public void onPacket(EventPacket.Receive event) {
		if (event.getPacket() instanceof ClientboundPlayerChatPacket packet) {
			if (Minecraft.getInstance().player == null) return;
			Minecraft mc = Minecraft.getInstance();

			String contents = packet.body().content();
			UUID sender_uuid = packet.sender();
			if (Minecraft.getInstance().player.getUUID().compareTo(sender_uuid) == 0 && this.ignoreSelf.getValue()) {
				return;
			}

			String sender_name = "ERROR";
			if (!playerCache.containsKey(sender_uuid)) {
				sender_name = fetchProfile(sender_uuid.toString());
				if (sender_name == null) {
					sender_name = sender_uuid.toString();
				}
				playerCache.put(sender_uuid, sender_name);
			} else {
				sender_name = playerCache.get(sender_uuid);
			}

			if (this.option.getValue() == OptionEnum.All) {
				String finalSender_name = sender_name;
				CompletableFuture.runAsync(() -> {
					String[] weebhooks = this.weebHook.getValue().split(",");
					for (String weebhook : weebhooks) {
						weebhook = weebhook.trim();
						sendHookFromPlayer(finalSender_name, sender_uuid ,contents, weebhook);
					}
				});
			} else {
				String[] players;
				if (this.players.getValue().isEmpty()) {
					return;
				} else {
					if (this.players.getValue().contains(",")) {
						players = this.players.getValue().split(",");
					} else {
						players = new String[]{this.players.getValue()};
					}
				}
				for (String player : players) {
					player = player.trim().toLowerCase();
					if (sender_name.toLowerCase().equals(player)) {
						String finalSender_name = sender_name;
						CompletableFuture.runAsync(() -> {
							String[] weebhooks = this.weebHook.getValue().split(",");
							for (String weebhook : weebhooks) {
								weebhook = weebhook.trim();
								sendHookFromPlayer(finalSender_name, sender_uuid ,contents, weebhook);
							}
						});
						break;
					}
				}
			}
		}
	}

	public void sendHookFromPlayer(String player,UUID uuid ,String msg, String weebhook) {
		try {
			int c = (color.getRed() << 16) + (color.getGreen() << 8) + (color.getBlue());
			String body = "{\"embeds\": [{\"title\": \"" + player + " mentioned\",\"description\": \"" + msg + "\",\"color\": \"" + c + "\"";

			String baseurl = ",\"thumbnail\": {\"url\":\"https://mc-heads.net";
			String normalend = "\"}]}";
			String end = "\"}}]}";

			if (avatar.getValue()) {
				body += baseurl;
				switch (avatarType.getValue()) {
					case HEAD -> body += "/head";
					case BODY -> body += "/body";
					case PLAYER -> body += "/player";
					case COMBO -> body += "/combo";
					case SKIN -> body += "/skin";
				}
				body += "/" + uuid.toString().replace("-", "");
				body += "/" + avatarSize.getValue();
				if (nohelm.getValue()) body += "/nohelm";
				if (avatarType.getValue() == AvatarEnum.BODY) {
					switch (bodylr.getValue()) {
						case LEFT -> body += "/left";
						case RIGHT -> body += "/right";
					}
				}
				body += end;
			} else {
				body += normalend;
			}

			makeAndSendHook(c, body, weebhook);
		} catch (Exception e) {
			this.getLogger().error("Failed to send webhook", e);
		}
	}

	private void makeAndSendHook(int c, String body, String weebhook) throws IOException {
		this.getLogger().debug("Color: " + c);
		this.getLogger().debug("Red: " + color.getRed() + " Green: " + color.getGreen() + " Blue: " + color.getBlue());

		HttpsURLConnection connection = (HttpsURLConnection) new URL(weebhook).openConnection();
		connection.addRequestProperty("Content-Type", "application/json");
		connection.addRequestProperty("User-Agent", "kybe236/2.3.6");
		connection.addRequestProperty("Content-Length", String.valueOf(body.length()));
		connection.setDoOutput(true);
		connection.setRequestMethod("POST");
		sendData(body, connection);
	}

	private void sendData(String body, HttpsURLConnection connection) throws IOException {
		connection.setConnectTimeout(1000);

		OutputStream stream = connection.getOutputStream();
		stream.write(body.getBytes());
		stream.flush();
		stream.close();

		connection.getInputStream().close();
		connection.disconnect();

		this.getLogger().info("Code: " + connection.getResponseCode());
	}

	public static String fetchProfile(String uuid) {
		try {
			String url = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid;

			// Create a URL object
			URL obj = new URL(url);

			// Open a connection
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// Set the request method to GET
			con.setRequestMethod("GET");

			// Add request headers if needed (e.g., User-Agent)
			con.setRequestProperty("User-Agent", "Mozilla/5.0");

			// Get the response code
			int responseCode = con.getResponseCode();

			if (responseCode == HttpURLConnection.HTTP_OK) { // success
				// Create an InputStreamReader to read the response
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuilder response = new StringBuilder();

				// Read the response line by line
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				// Convert the response to a JSON object
				JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();

				// Extract the id and name from the JSON response
				String name = jsonResponse.get("name").getAsString();

				// Create a new MinecraftProfile object and return it
				return name;

			} else {
				System.out.println("GET request failed: " + responseCode);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Return null if the request failed
		return null;
	}
}

