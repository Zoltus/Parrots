package sh.zoltus.parrots.player;

import lombok.Getter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Parrot.Variant;
import org.bukkit.entity.Player;
import sh.zoltus.parrots.Parrots;

@Getter
public class Pet {

	private final Parrots plugin = Parrots.getPlugin();


	private final Parrot parrot;

	@SuppressWarnings("deprecation")
	public Pet(Player p, Variant color) {
		parrot = (Parrot) p.getWorld().spawnEntity(p.getLocation(), EntityType.PARROT);
		parrot.remove();
		parrot.setSilent(plugin.getYml().getBoolean("Config.isSilent"));
		parrot.setCustomNameVisible(false);
		parrot.setVariant(color);
		parrot.setSitting(true);
		parrot.setOwner(p);
		parrot.setTamed(true);
		parrot.setAI(true);
	}
}
