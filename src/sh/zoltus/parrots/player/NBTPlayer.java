package sh.zoltus.parrots.player;

import lombok.SneakyThrows;
import net.minecraft.nbt.*;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;


public class NBTPlayer {

    private final File datFile;
    private NBTTagCompound compound;

    public NBTPlayer(OfflinePlayer offP) {
        this(offP.getUniqueId());
    }

    //todo cleanup
    private NBTPlayer(UUID uuid) {
        Validate.notNull(uuid, "Cannot get null UUID");
        World w = Bukkit.getWorlds().get(0);
        datFile = new File(w.getWorldFolder().getAbsolutePath() + "/playerdata/" + uuid.toString().toLowerCase() + ".dat");

        if (!datFile.exists()) {
            System.out.println("File does not exist: " + datFile.getAbsolutePath());
        } else if (datFile.isDirectory()) {
            System.out.println("File is a directory not player.dat file: " + datFile.getAbsolutePath());
        } else {
            try {
                compound = NBTCompressedStreamTools.a(new FileInputStream(datFile));
            } catch (IOException ex) {
                System.out.println("Error trying to create NBTTagCompund for player : " + uuid + " :" + ex.getMessage());
            }
        }
    }

    @SneakyThrows
    public void save() {
        NBTCompressedStreamTools.a(compound, datFile);
    }

    //TODO add all attributes for player, luck armortoughtness ect
    @SuppressWarnings("SameParameterValue")
    private Double getAttribute(String abilityName) {
        NBTTagList list = compound.c("Attributes", 10);
        for (NBTBase nbt : list) {
            NBTTagCompound attribute = (NBTTagCompound) nbt;
            if (attribute.l("Name").equals(abilityName)) {
                return attribute.k("Base");
            }
        }
        return null;
    }

    public NBTTagCompound getShoulderLeft() {
        return compound.p("ShoulderEntityLeft");
    }

    public void setShoulderLeft(NBTTagCompound compoundd) {
        compound.a("ShoulderEntityLeft", compoundd);
    }
}
