package net.flares.flaremobcoins.service.menu;

import lombok.Getter;
import net.flarepowered.neo.ui.items.FlareStack;
import net.flares.flaremobcoins.files.FilesManager;

@Getter
public class RotatingItem {

    private StockType stockType;
    private String itemID;
    private FlareStack stack;

    private int maxStock;
    private int currentStock;

    public RotatingItem (StockType stockType, int maxStock, FlareStack stack, String itemID) {
        this.stack = stack;
        this.stockType = stockType;
        this.maxStock = this.currentStock = maxStock;
        this.itemID = itemID;
    }

    public RotatingItem (StockType stockType, int maxStock, int currentStock, FlareStack stack, String itemID) {
        this.stack = stack;
        this.stockType = stockType;
        this.maxStock = maxStock;
        this.currentStock = currentStock;
        this.itemID = itemID;
    }

    public int getStock () {
        return currentStock;
    }

    public void updateStock(int newStock, String shopID, String itemType) {
        this.currentStock = newStock;
        FilesManager.ACCESS.getData().getConfig().set("rotating_shop." + shopID + "." + itemType + "_items." + itemID + ".stock", newStock);
        FilesManager.ACCESS.getData().saveConfig();
    }

    public enum StockType {
        SERVER,
        PLAYER
    }
}
