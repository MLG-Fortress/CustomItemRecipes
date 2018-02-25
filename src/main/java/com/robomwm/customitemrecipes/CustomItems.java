package com.robomwm.customitemrecipes;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2/24/2018.
 *
 * @author RoboMWM
 */
class CustomItems implements CommandExecutor
{
    private CustomItemRecipes customItemRecipes;
    private YamlConfiguration itemsYaml;
    CustomItems(CustomItemRecipes customItemRecipes)
    {
        this.customItemRecipes = customItemRecipes;
        File itemsFile = new File(customItemRecipes.getDataFolder(), "items.yml");
        if (!itemsFile.exists())
        {
            try
            {
                itemsFile.createNewFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return;
            }
        }
        itemsYaml = YamlConfiguration.loadConfiguration(itemsFile);

        for (String itemString : itemsYaml.getKeys(false))
        {
            ConfigurationSection section = itemsYaml.getConfigurationSection(itemString);
            customItemRecipes.registerItem((ItemStack)section.get(itemString), itemString);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (args.length == 0)
            return false;
        if (args.length < 2)
        {
            switch(args[0].toLowerCase())
            {
                case "lore":
                    sender.sendMessage("/" + cmd.getLabel() + " lore <clear/add/set> <lore...>");
                    break;
                case "name":
                    sender.sendMessage("/" + cmd.getLabel() + "name <name...> - Sets display name of item. Color codes accepted.");
                    break;
                case "register":
                    sender.sendMessage("/" + cmd.getLabel() + "register <name...> - Adds custom item to plugin with the given name as its ID, storing it and allowing recipes to be created for it. Color codes accepted but not recommended.");
                    break;
                case "get":
                    sender.sendMessage("/" + cmd.getLabel() + "<get> <customitem name...> - Adds custom item to your inventory.");
                    break;
            }
            return true;
        }
        if (!(sender instanceof Player))
            return false;
        Player player = (Player)sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR)
            return false;

        ItemMeta itemMeta = item.getItemMeta();

        switch (args[0].toLowerCase())
        {
            case "lore":
                List<String> lore = null;
                switch(args[1].toLowerCase())
                {
                    case "remove":
                    case "clear":
                        itemMeta.setLore(null);
                        break;
                    case "add":
                        if (itemMeta.hasLore())
                            lore = itemMeta.getLore();
                    case "set":
                        if (lore == null)
                            lore = new ArrayList<>();
                        args[0] = null;
                        args[1] = null;
                        for (String arg : args)
                        {
                            if (arg != null)
                                lore.add(ChatColor.translateAlternateColorCodes('&', arg));
                        }
                        itemMeta.setLore(lore);
                        break;
                    default:
                        sender.sendMessage("/" + cmd.getLabel() + " lore <clear/add/set> <lore...>");
                }
                break;
            case "name":
                args[0] = null;
                itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', StringUtils.join(args, " ")));
                break;
            case "register":
                if (!customItemRecipes.registerItem(item, ChatColor.translateAlternateColorCodes('&', args[0])))
                {
                    sender.sendMessage("Already registered");
                    return false;
                }
                itemsYaml.set(args[0], item);
                return true;
            case "get":
                ItemStack itemStack = customItemRecipes.getItem(ChatColor.translateAlternateColorCodes('&', StringUtils.join(args, " ")));
                if (itemStack != null)
                    player.getInventory().addItem(itemStack);
        }

        item.setItemMeta(itemMeta);
        player.getInventory().setItemInMainHand(item);
        return true;
    }
}
