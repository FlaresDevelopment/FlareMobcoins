package net.flares.flaremobcoins.API;

import net.flares.flaremobcoins.service.ServiceHandler;
import net.flares.flaremobcoins.util.Utils;

import java.util.UUID;

public class MobcoinsPlayer {

    UUID uuid;
    double mobcoins;
    double multiplier = 1;

    public static MobcoinsPlayer warpPlayer(UUID uuid) {
        // get player if the player don't exist (in cache)
        if(ServiceHandler.SERVICE.getDataService().cache.containsKey(uuid)) {
            return ServiceHandler.SERVICE.getDataService().cache.get(uuid);
        } else {
            if(ServiceHandler.SERVICE.getDataService().hasAccount(uuid)) {
                return new MobcoinsPlayer(uuid, ServiceHandler.SERVICE.getDataService().getMobcoins(uuid), ServiceHandler.SERVICE.getDataService().getMultiplier(uuid));
            } else {
                return new MobcoinsPlayer(uuid, ServiceHandler.SERVICE.getDataService().firstMobcoins, 1);
            }
        }
    }
    public static MobcoinsPlayer warpPlayer(String name) {
        UUID uuid = Utils.UTILS.getPlayerUUID(name);
        // get player if the player don't exist (in cache)
        if(ServiceHandler.SERVICE.getDataService().cache.containsKey(uuid)) {
            return ServiceHandler.SERVICE.getDataService().cache.get(uuid);
        } else {
            if(ServiceHandler.SERVICE.getDataService().hasAccount(uuid)) {
                return new MobcoinsPlayer(uuid, ServiceHandler.SERVICE.getDataService().getMobcoins(uuid), ServiceHandler.SERVICE.getDataService().getMultiplier(uuid));
            } else {
                return new MobcoinsPlayer(uuid, ServiceHandler.SERVICE.getDataService().firstMobcoins, 1);
            }
        }
    }

    public MobcoinsPlayer() {
        this.mobcoins = 0;
    }

    public MobcoinsPlayer(UUID uuid, double mobcoins, double multiplier) {
        this.uuid = uuid;
        this.mobcoins = mobcoins;
        this.multiplier = multiplier;
    }

    public UUID getPlayer() {
        return uuid;
    }

    public void setPlayer(UUID uuid) {
        this.uuid = uuid;
    }

    public double getMobcoins() {
        return mobcoins;
    }

    /**
     * If you have the MobcoinsPlayer you can set his
     * mobcoins balance to whatever you want
     * @param mobcoins A <bold>double</bold> number.
     */
    public void setMobcoins(double mobcoins) {
        this.mobcoins = mobcoins;
        if(uuid == null) return;
        ServiceHandler.SERVICE.getDataService().setMobcoins(uuid, this.mobcoins);

    }

    public void objectSetMobcoins(double mobcoins) {
        this.mobcoins = mobcoins;
    }

    public void objectSetMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    /**
     * If you have the MobcoinsPlayer you can give a
     * player how many tokens you want
     * @param mobcoins A <bold>double</bold> number.
     */
    public void giveMobcoins(double mobcoins) {
        this.mobcoins = this.mobcoins + mobcoins;
        if(uuid == null) return;
        ServiceHandler.SERVICE.getDataService().setMobcoins(uuid, this.mobcoins);
    }
    /**
     * If you have the MobcoinsPlayer you can remove
     * tokens from the player!
     * @param mobcoins A <bold>double</bold> number.
     */
    public void removeMobcoins(double mobcoins) {
        this.mobcoins = this.mobcoins - mobcoins;
        if(uuid == null) return;
        ServiceHandler.SERVICE.getDataService().setMobcoins(uuid, this.mobcoins);
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
        if(uuid == null) return;
        ServiceHandler.SERVICE.getDataService().setMultiplier(uuid, this.multiplier);
    }
}
