package sh.zoltus.parrots.player.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;
import sh.zoltus.parrots.Parrots;
import sh.zoltus.parrots.configuration.OneYml;
import sh.zoltus.parrots.gui.ParrotGui;
import sh.zoltus.parrots.player.Holder;

public class ParrotsCMDApi extends CommandAPICommand {

    private final OneYml yml = Parrots.getYml();
    private final String ADMIN_PERM = "parrots.admin";
    //todo holder argument

    public ParrotsCMDApi() {
        super("parrots");
        withAliases(yml.getStringList("Config.aliases").toArray(new String[0]));
        withPermission("parrots.menu");
        executesPlayer((sender, args) -> {
            ParrotGui gui = ParrotGui.get(sender);
            gui.show();
        });

        CommandAPICommand version = new CommandAPICommand("version")
                .executes((sender, args) -> {
                    sender.sendMessage("By Zoltus");
                    sender.sendMessage("" + "1123123123"); //todo
                    sender.sendMessage("Profile: https://www.spigotmc.org/members/zoltus.306747/");
                });
        CommandAPICommand reload = new CommandAPICommand("reload")
                .withPermission(ADMIN_PERM)
                .executes((sender, args) -> {
                    yml.reload();
                    sender.sendMessage(yml.getMessage("Messages.Reloaded"));
                });

        CommandAPICommand set = new CommandAPICommand("set")
                .withPermission(ADMIN_PERM)
                .withArguments(new PlayerArgument("player"))
                .withArguments(new MultiLiteralArgument("both", "right", "left"))
                .withArguments(new MultiLiteralArgument("red", "blue", "green", "cyan", "gray"))
                .executes((sender, args) -> {
                    Holder target = Holder.of((Player) args[0]);
                    Holder.Shoulder shoulder = Holder.Shoulder.valueOf(args[0].toString().toUpperCase());
                    Parrot.Variant color = Parrot.Variant.valueOf(args[0].toString().toUpperCase());
                    target.setParrot(shoulder, color);
                    sender.sendMessage(yml.getMessage("Messages.ParrotSet"));
                });

        CommandAPICommand remove = new CommandAPICommand("remove")
                .withPermission(ADMIN_PERM)
                .withArguments(new PlayerArgument("player"))
                .executes((sender, args) -> {
                    Holder target = Holder.of((Player) args[0]);
                    if (target.hasFakeParrots()) {
                        target.removeFakeParrots();
                        sender.sendMessage(yml.getMessage("Messages.YouRemovedParrot")
                                .replaceAll("%PLAYER%", target.player().getName()));
                    } else {
                        sender.sendMessage(yml.getMessage("Messages.PlayerDoesNotHaveParrot"));
                    }
                });


        withSubcommands(version, set, reload, remove);
    }
}