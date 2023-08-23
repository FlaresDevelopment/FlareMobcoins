package net.flares.flaremobcoins;

import org.bukkit.plugin.java.JavaPlugin;

public class MobcoinsByFlares extends JavaPlugin {

  @Override
  public void onEnable() {
    FlareMobcoins.PLUGIN.start(this);
  }

  @Override
  public void onDisable() {
    FlareMobcoins.PLUGIN.stop(this);
  }
}
