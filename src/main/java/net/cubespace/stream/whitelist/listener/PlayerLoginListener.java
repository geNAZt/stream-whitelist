package net.cubespace.stream.whitelist.listener;

import net.cubespace.stream.whitelist.Whitelist;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * Created by Fabian on 24.06.15.
 */
public class PlayerLoginListener implements Listener {
    @EventHandler
    public void onLogin( LoginEvent event ) {
        if ( !Whitelist.getInstance().getWhitelistEntityManager().isWhitelisted( event.getConnection().getUniqueId() ) ) {
            event.setCancelled( true );
            event.setCancelReason( "Du bist nicht gewhitelisted" );
        }
    }
}
