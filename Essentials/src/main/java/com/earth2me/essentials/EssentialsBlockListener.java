package com.earth2me.essentials;

import com.earth2me.essentials.utils.LocationUtil;
import com.earth2me.essentials.utils.MaterialUtil;
import net.ess3.api.IEssentials;
import org.bukkit.GameMode;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;


public class EssentialsBlockListener implements Listener {
    private final IEssentials ess;

    public EssentialsBlockListener(final IEssentials ess) {
        this.ess = ess;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockPlace(final BlockPlaceEvent event) {
        // Do not rely on getItemInHand();
        // http://leaky.bukkit.org/issues/663
        final ItemStack is = LocationUtil.convertBlockToItem(event.getBlockPlaced());

        if (is == null) {
            return;
        }

        if (is.getType() == MaterialUtil.SPAWNER && event.getItemInHand().getType() == MaterialUtil.SPAWNER) {
            final BlockState blockState = event.getBlockPlaced().getState();
            if (blockState instanceof CreatureSpawner) {
                final CreatureSpawner spawner = (CreatureSpawner) blockState;
                final EntityType type = ess.getSpawnerProvider().getEntityType(event.getItemInHand());
                final Mob mob = Mob.fromBukkitType(type);
                if (type != null && mob != null) {
                    String mobName = mob.name().toLowerCase(Locale.ENGLISH);
                    if (ess.getUser(event.getPlayer()).isAuthorized("essentials.spawnerconvert." + mobName)) {
                        spawner.setSpawnedType(type);
                        spawner.update();
                    }
                }
            }
        }

        final User user = ess.getUser(event.getPlayer());
        if (user.hasUnlimited(is) && user.getBase().getGameMode() == GameMode.SURVIVAL) {
            class UnlimitedItemSpawnTask implements Runnable {
                @Override
                public void run() {
                    user.getBase().getInventory().addItem(is);
                    user.getBase().updateInventory();
                }
            }
            ess.scheduleSyncDelayedTask(new UnlimitedItemSpawnTask());
        }
    }
}