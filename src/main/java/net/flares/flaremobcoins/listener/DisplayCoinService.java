package net.flares.flaremobcoins.listener;

import net.flarepowered.core.TML.FlareScript;
import net.flarepowered.core.TML.components.player.SoundComponent;
import net.flarepowered.neo.ui.items.FlareMaterial;
import net.flarepowered.neo.ui.items.FlareStack;
import net.flarepowered.other.Logger;
import net.flarepowered.other.exceptions.ItemBuilderConfigurationException;
import net.flarepowered.utils.HeadUtils;
import net.flares.flaremobcoins.API.MobCoinReceiveEvent;
import net.flares.flaremobcoins.FlareMobcoins;
import net.flares.flaremobcoins.files.FilesManager;
import net.flares.flaremobcoins.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DisplayCoinService implements Listener {

    public final boolean isEnabled = FilesManager.ACCESS.getConfig().getConfig().getBoolean("display_coin.enable");
    private ItemStack stack;
    private final NamespacedKey keyForPlayer = new NamespacedKey(FlareMobcoins.PLUGIN.getPlugin(), "dsp_coin_player");
    private final NamespacedKey keyForAmount = new NamespacedKey(FlareMobcoins.PLUGIN.getPlugin(), "dsp_coin_amount");

    public void onEnable() {
        if(!isEnabled) return;
        try {
            this.stack = FlareMaterial.wrapFromString(FilesManager.ACCESS.getConfig().getConfig().getString("display_coin.display_material")).construct(null);
        } catch (ItemBuilderConfigurationException e) {
            Logger.error("The we could not build the material for the display item! Error: " + e.getMessage());
            return;
        }
        FlareMobcoins.PLUGIN.getPlugin().getServer().getPluginManager().registerEvents(this, FlareMobcoins.PLUGIN.getPlugin());
    }

    public void dropItemToGround(double amount, Player owner, Location dropzone) {
        Collection<Entity> itemsSearch = owner.getWorld().getNearbyEntities(dropzone, 2, 1, 2);
        for(Entity e : itemsSearch) {
            if(e instanceof Item) {
                ItemStack stack = ((Item) e).getItemStack();
                if(!stack.hasItemMeta()) continue;
                PersistentDataContainer ps = stack.getItemMeta().getPersistentDataContainer();
                if(!ps.has(keyForAmount)) continue;
                if(ps.has(keyForPlayer))
                    if(!Objects.equals(ps.get(keyForPlayer, PersistentDataType.STRING), owner.getName())) continue;
                amount += ps.get(keyForAmount, PersistentDataType.DOUBLE);
                e.remove();
            }
        }
        ItemStack is = new ItemStack(stack);
        ItemMeta im = is.getItemMeta();
        im.getPersistentDataContainer().set(keyForAmount, PersistentDataType.DOUBLE, amount);
        im.getPersistentDataContainer().set(keyForPlayer, PersistentDataType.STRING, owner.getName());
        im.setDisplayName(amount + "");
        is.setItemMeta(im);
        Item item = owner.getWorld().dropItem(dropzone.add(0, 0.1, 0), is);
        item.setInvulnerable(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void itemPickupCheck(EntityPickupItemEvent event) {
        if(!event.getItem().getItemStack().hasItemMeta()) return;
        if(!event.getItem().getItemStack().getItemMeta().getPersistentDataContainer().has(keyForAmount)) return;
        if(!(event.getEntity() instanceof Player)) {
            event.setCancelled(true);
        }
        double toGive = Double.parseDouble(Utils.UTILS.getTwoDecimals().format(
                event.getItem().getItemStack().getItemMeta().getPersistentDataContainer().get(keyForAmount, PersistentDataType.DOUBLE)));
        event.setCancelled(true);
        event.getItem().remove();
        new FlareScript().processFull((List<String>) FilesManager.ACCESS.getConfig().getConfig().getStringList("display_coin.pickup_commands")
                .stream().map(s -> s.replace("%pl_mobcoins%", toGive + ""))
                .collect(Collectors.toCollection(ArrayList::new)), (Player) event.getEntity());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void hopperPickup(InventoryPickupItemEvent event) {
        if(!event.getItem().getItemStack().hasItemMeta()) return;
        if(!event.getItem().getItemStack().getItemMeta().getPersistentDataContainer().has(keyForAmount)) return;
        event.setCancelled(true);
    }

}
