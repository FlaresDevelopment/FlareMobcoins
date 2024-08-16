package net.flares.flaremobcoins.service.menu;

import lombok.Getter;
import lombok.Setter;
import net.flarepowered.core.TML.FlareScript;
import net.flarepowered.core.text.Message;
import net.flarepowered.core.text.other.Replace;
import net.flarepowered.neo.ui.MenuArchitect;
import net.flarepowered.neo.ui.contents.InventoryScreen;
import net.flarepowered.neo.ui.contents.UI;
import net.flarepowered.neo.ui.contents.helper.UITemplate;
import net.flarepowered.utils.Utility;
import net.flares.flaremobcoins.service.Service;
import net.flares.flaremobcoins.util.FileWrapper;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Getter
@Setter
public class MemoryMenu {

    private byte[] checksum;
    private File file;
    private String id;
    private UITemplate template;

    private InventoryScreen screen;

    private List<String> openRequirements;
    private String permission;

    private RotatingShop rotatingMenu;

    private void validate() {
        try {
            if(checksum == null) {
                checksum = Utility.generateChecksum(file.getAbsolutePath(), "MD5");
                buildTemplate();
            }
            if (!Arrays.equals(checksum, Utility.generateChecksum(file.getAbsolutePath(), "MD5"))) {
                checksum = Utility.generateChecksum(file.getAbsolutePath(), "MD5");
                buildTemplate();
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private void buildTemplate() {
        FileConfiguration fileConfiguration = FileWrapper.wrapConfiguration(file);
        UI ui = UI.wrapMenuFromConfiguration(null, id, fileConfiguration);
        if(openRequirements != null) openRequirements.clear();
        else openRequirements = new ArrayList<>();
        if(fileConfiguration.contains("open_requirements")) {
            openRequirements.addAll(fileConfiguration.getStringList("open_requirements"));
        }
        if(fileConfiguration.contains("open_requirement"))
            openRequirements.add(fileConfiguration.getString("open_requirement"));
        if(fileConfiguration.contains("menu_permission"))
            permission = fileConfiguration.getString("menu_permission");
        template = ui.exportPanel();
    }

    public boolean checkRequirements(Player player, Replace... replaces) {
        validate();
        if(openRequirements == null) return true;
        if(openRequirements.isEmpty()) return true;
        return new FlareScript().processFull(Message.format(permission, player, replaces), player);
    }

    public boolean checkPermission(Player player, Replace... replaces) {
        validate();
        if(permission == null) return true;
        return player.hasPermission(Message.format(permission, player, replaces));
    }

    public void showMenu(Player player, Replace... replaces) {
        validate();
        UI ui = UI.wrapFromTemplate(player, template, true);
        // do stuff with ui here!
        if(rotatingMenu != null) rotatingMenu.injectIntoUI(ui);
        MenuArchitect.MENU.openInventory(player, ui);
        Service.SERVICE.getMenuService().getInMenus().put(player, id);
    }

}
