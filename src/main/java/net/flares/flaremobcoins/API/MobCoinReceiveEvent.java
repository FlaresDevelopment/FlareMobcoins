package net.flares.flaremobcoins.API;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public class MobCoinReceiveEvent extends Event implements Cancellable {

    private static Field asyncField;
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private Player player;
    private MobcoinsPlayer mobCoinsPlayer;
    private String entity;
    private boolean isCancelled;
    private double obtainedAmount;

    public MobCoinReceiveEvent(Player player, MobcoinsPlayer mobCoinsPlayer, String entity, double obtainedAmount) {
        this.player = player;
        this.mobCoinsPlayer = mobCoinsPlayer;
        this.entity = entity;
        this.obtainedAmount = obtainedAmount;
        this.isCancelled = false;
//        try {
//            asyncField.set(this, !Bukkit.isPrimaryThread());
//        } catch (IllegalAccessException var5) {
//            var5.printStackTrace();
//        }
    }

//    static {
//        try {
//            asyncField = Event.class.getDeclaredField("async");
//            asyncField.setAccessible(true);
//        } catch (NoSuchFieldException var1) {
//            var1.printStackTrace();
//        }
//
//    }


    public Player getPlayer() { return player; }

    public double getObtainedAmount() { return obtainedAmount;  }

    public String getEntity() { return entity; }

    public void setDropAmount(double amount) { this.obtainedAmount = amount; }

    public MobcoinsPlayer getMobCoinsPlayer() { return mobCoinsPlayer; }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
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
}
