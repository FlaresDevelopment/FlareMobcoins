package net.flares.flaremobcoins.util;

public class ShopStock {
    int stock;
    StockType type;

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public StockType getType() {
        return type;
    }

    public void setType(StockType type) {
        this.type = type;
    }

    public enum StockType {
        SERVER,
        PLAYER
    }
}
