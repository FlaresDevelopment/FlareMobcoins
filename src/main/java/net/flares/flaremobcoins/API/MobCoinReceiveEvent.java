package net.flares.flaremobcoins.API;

import lombok.Getter;
import net.flares.flaremobcoins.drops.EntityConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

@Getter
public class MobCoinReceiveEvent extends Event implements Cancellable {

    private static Field asyncField;
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private Player player;
    private MobcoinsPlayer mobCoinsPlayer;
    private String entity;
    private boolean isCancelled;
    private EntityConfiguration entityConfiguration;
    private double droppedAmount;

    public MobCoinReceiveEvent(Player player, MobcoinsPlayer mobCoinsPlayer, String entity, EntityConfiguration entityConfiguration, double droppedAmount) {
        this.player = player;
        this.mobCoinsPlayer = mobCoinsPlayer;
        this.entity = entity;
        this.entityConfiguration = entityConfiguration;
        this.droppedAmount = droppedAmount;
        this.isCancelled = false;
    }


    @Override
    public boolean isCancelled() { return false; }

    @Override
    public void setCancelled(boolean cancel) { this.isCancelled = cancel; }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
