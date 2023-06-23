package net.devtm.tmmobcoins.util;

import org.bukkit.entity.Player;

public class StockProfile {
    int leftStock;
    int maxStock;
    ShopStock.StockType type;
    // This player will be used only if the stop Type is a player type
    Player player;

    public StockProfile(int leftStock, ShopStock.StockType type, Player player, int maxStock) {
        this.leftStock = leftStock;
        this.type = type;
        this.player = player;
        this.maxStock = maxStock;
    }

    public int getMaxStock() {
        return maxStock;
    }

    public void setMaxStock(int maxStock) {
        this.maxStock = maxStock;
    }

    public int getStock() {
        return leftStock;
    }

    public void setStock(int stock) {
        this.leftStock = stock;
    }

    public ShopStock.StockType getType() {
        return type;
    }

    public void setType(ShopStock.StockType type) {
        this.type = type;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}
