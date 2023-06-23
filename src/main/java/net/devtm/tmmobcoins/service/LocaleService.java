package net.devtm.tmmobcoins.service;

public enum LocaleService {
    BASIC_no_permission("basic.no_permission"),
    BASIC_some_error("basic.some_error"),
    BASIC_player_not_found("basic.player_not_found"),
    BASIC_only_player("basic.only_player"),
    CMD_RELOAD_success("commands.reload.success");

    public String get;
    LocaleService(String get) {
        this.get = get;
    }
}
