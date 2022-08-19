package sh.zoltus.parrots.player.commands;

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import sh.zoltus.parrots.Parrots;
import sh.zoltus.parrots.configuration.OneYml;
import sh.zoltus.parrots.gui.ParrotGui;
import sh.zoltus.parrots.player.Holder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParrotsCMD extends Command {
    private final OneYml yml = Parrots.getYml();

    public ParrotsCMD() {
        super("parrots");
        this.setAliases(yml.getOrDefault("aliases", new ArrayList<>()));
        this.description = "Parrots related commands";
        this.usageMessage = "/parrots <subcmd>";
    }

    public boolean execute(@NonNull CommandSender sender, @NonNull String commandLabel, String @NonNull [] args) {
        if (args.length == 0 && sender instanceof Player) { // /Parrots
            Player player = (Player) sender;
            ParrotGui parrotGui = ParrotGui.get(player);
            parrotGui.show();
        } else if (args.length > 0) {
            String args0 = args[0];
            if (args0.equals("version")) {
                sender.sendMessage("By Zoltus");
                sender.sendMessage("" + "1123123123"); //todo
                sender.sendMessage("Profile: https://www.spigotmc.org/members/zoltus.306747/");
            }  if (args.length == 4 && args0.equals("set")) {
                Holder holder = getHolder(sender, args0);
                //todo shoulder, tabcomplete shoulder checks, switch to commandapi?
                if (holder != null) {
                    sender.sendMessage(yml.getMessage("Messages.ParrotSet"));
                }
            } else if (args.length == 2 && args0.equals("remove")) {
                Holder holder = getHolder(sender, args0);
                if (holder != null) {
                    if (holder.removeFakeParrot(Holder.Shoulder.BOTH)) {
                        sender.sendMessage(yml.getMessage("Messages.YouRemovedParrot")
                                .replaceAll("%PLAYER%", holder.getPlayer().getName()));
                    } else {
                        sender.sendMessage(yml.getMessage("Messages.PlayerDoesNotHaveParrot"));
                    }
                }
            } else  {
                sender.sendMessage(yml.getMessage("Messages.Usage"));
            }
            //todo admin perm check cleanup
        }
        return true;
    }


    private Holder getHolder(CommandSender sender, String arg) {
        Player target = Bukkit.getPlayer(arg);
        if (target != null) {
            return Holder.of(target);
        } else {
            //todo stafck with set cmd method
            sender.sendMessage(yml.getMessage("Messages.PlayerNotFound"));
        }
        return null;
    }


    private boolean hasAdminPerm(CommandSender sender) {
        if (sender.hasPermission("Parrots.Admin")) {
            return true;
        } else {
            sender.sendMessage(yml.getMessage("Messages.NoPermissions"));
            return false;
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandSender sender, @NonNull String alias, String @NonNull [] args) throws IllegalArgumentException {
        return Arrays.asList("asd", "asdad");
    }
}


    /*


  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (!cmd.getName().equalsIgnoreCase("parrots"))
      return false;
    if (args.length == 0) {
      if (!isPlayer(sender))
        return false;
      Player p = (Player)sender;
      if (p.hasPermission("Parrots.Menu")) {
        GUI.ParrotMenu(p);
      } else {
        sender.sendMessage((String)getConfig().get("Messages.NoPermissions"));
      }
      return true;
    }
    switch (args[0].toLowerCase()) {
      case "reload":
        if (sender.hasPermission("Parrots.Admin")) {
          sender.sendMessage((String)getConfig().get("Messages.Reloaded"));
          saveDefaultConfig();
          reloadConfig();
        } else {
          sender.sendMessage((String)getConfig().get("Messages.NoPermissions"));
        }
        return true;
      case "version":
        sender.sendMessage("By Zoltus");
        sender.sendMessage("" + this.version);
        sender.sendMessage("Profile: https://www.spigotmc.org/members/zoltus.306747/");
        return true;
      case "set":
        if (args.length == 4) {
          if (Bukkit.getPlayer(args[1]) == null) {
            sender.sendMessage((String)getConfig().get("Messages.PlayerNotFound"));
            return false;
          }
          if (isPlayer(sender) && !sender.hasPermission("Parrots.Admin")) {
            sender.sendMessage((String)getConfig().get("Messages.NoPermissions"));
            return false;
          }
          Player p2 = Bukkit.getPlayer(args[1]);
          ParrotPet.Shoulder side2 = ParrotPet.Shoulder.valueOf(args[2].toUpperCase());
          Parrot.Variant variant = Parrot.Variant.valueOf(args[3].toUpperCase());
          if (ParrotHandler.getParrotPet(p2) != null)
            ParrotHandler.getParrotPet(p2).removeParrot();
          new ParrotPet(p2, variant, side2);
          sender.sendMessage((String)getConfig().get("Messages.ParrotSet"));
        } else {
          sender.sendMessage((String)getConfig().get("Messages.InvalidArguments"));
        }
        return true;
      case "remove":
        if (args.length == 2) {
          if (sender.hasPermission("Parrots.Admin")) {
            Player p2 = Bukkit.getPlayer(args[1]);
            if (p2 != null) {
              if (ParrotHandler.getParrotPet(p2) != null) {
                ParrotHandler.getParrotPet(p2).removeParrot();
                sender.sendMessage(getConfig().get("Messages.YouRemovedParrot").toString().replaceAll("%PLAYER%", p2.getName()));
              } else {
                sender.sendMessage((String)getConfig().get("Messages.PlayerDoesNotHaveParrot"));
              }
            } else {
              sender.sendMessage((String)getConfig().get("Messages.PlayerNotFound"));
            }
          } else {
            sender.sendMessage((String)getConfig().get("Messages.NoPermissions"));
          }
        } else {
          sender.sendMessage((String)getConfig().get("Messages.InvalidArguments"));
        }
    }
    sender.sendMessage((String)getConfig().get("Messages.InvalidArguments"));
    return true;
  }

  public boolean isPlayer(CommandSender sender) {
    if (sender instanceof Player)
      return true;
    sender.sendMessage((String)getConfig().get("Messages.PlayerOnlyCommand"));
    return false;
  }
     */
