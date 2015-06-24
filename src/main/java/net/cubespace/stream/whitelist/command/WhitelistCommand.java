package net.cubespace.stream.whitelist.command;

import com.mojang.api.profiles.HttpProfileRepository;
import com.mojang.api.profiles.Profile;
import net.cubespace.stream.whitelist.Whitelist;
import net.cubespace.stream.whitelist.util.Callback;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.UUID;

/**
 * Created by Fabian on 24.06.15.
 */
public class WhitelistCommand extends Command {
    public WhitelistCommand() {
        super( "whitelist", "stream.whitelist" );
    }

    @Override
    public void execute( final CommandSender sender, String[] args ) {
        // Check if enough arguments are given
        if ( args.length < 1 ) {
            sender.sendMessage( new ComponentBuilder( "Du musst mindestens ein Argument angeben" ).color( ChatColor.DARK_RED ).create() );
            return;
        }

        // Subcommand selector
        switch ( args[0] ) {
            case "add":
                if ( args.length < 2 ) {
                    sender.sendMessage( new ComponentBuilder( "/whitelist add benötigt einen Spielernamen" ).color( ChatColor.DARK_RED ).create() );
                    return;
                }

                addToWhitelist( args[1], new Callback<Boolean>() {
                    @Override
                    public void done( Boolean doneValue ) {
                        if ( !doneValue ) {
                            sender.sendMessage( new ComponentBuilder( "Dieser Spieler ist kein gültiger Minecraftname" ).color( ChatColor.DARK_RED ).create() );
                        } else {
                            sender.sendMessage( new ComponentBuilder( "Dieser Spieler wurde der Whitelist hinzugefügt" ).color( ChatColor.GREEN ).create() );
                        }
                    }
                } );

                break;

            case "delete":
                if ( args.length < 2 ) {
                    sender.sendMessage( new ComponentBuilder( "/whitelist delete benötigt einen Spielernamen" ).color( ChatColor.DARK_RED ).create() );
                    return;
                }

                deleteFromWhitelist( args[1], new Callback<Boolean>() {
                    @Override
                    public void done( Boolean doneValue ) {
                        if ( !doneValue ) {
                            sender.sendMessage( new ComponentBuilder( "Dieser Spieler ist kein gültiger Minecraftname" ).color( ChatColor.DARK_RED ).create() );
                        } else {
                            sender.sendMessage( new ComponentBuilder( "Dieser Spieler wurde von der Whitelist entfernt" ).color( ChatColor.GREEN ).create() );
                        }
                    }
                } );
                break;

            case "check":
                if ( args.length < 2 ) {
                    sender.sendMessage( new ComponentBuilder( "/whitelist check benötigt einen Spielernamen" ).color( ChatColor.DARK_RED ).create() );
                    return;
                }

                checkWhitelisted( args[1], new Callback<Boolean>() {
                    @Override
                    public void done( Boolean doneValue ) {
                        if ( !doneValue ) {
                            sender.sendMessage( new ComponentBuilder( "Dieser Spieler ist kein gültiger Minecraftname oder nicht gewhitelisted" ).color( ChatColor.DARK_RED ).create() );
                        } else {
                            sender.sendMessage( new ComponentBuilder( "Dieser Spieler ist gewhitelisted" ).color( ChatColor.GREEN ).create() );
                        }
                    }
                } );
                break;

            default:
                sender.sendMessage( new ComponentBuilder( "Dieser Subcommand ist nicht bekannt. Folgende Commands sind verfügbar:" ).color( ChatColor.DARK_RED ).create() );
                sender.sendMessage( new ComponentBuilder( "/whitelist add" ).color( ChatColor.YELLOW ).append( " [name]" ).color( ChatColor.GREEN ).create() );
                sender.sendMessage( new ComponentBuilder( "/whitelist delete" ).color( ChatColor.YELLOW ).append( " [name]" ).color( ChatColor.GREEN ).create() );
                sender.sendMessage( new ComponentBuilder( "/whitelist check" ).color( ChatColor.YELLOW ).append( " [name]" ).color( ChatColor.GREEN ).create() );

                break;
        }
    }

    private void addToWhitelist( String user, final Callback<Boolean> successCallback ) {
        getUUIDForUser( user, new Callback<UUID>() {
            @Override
            public void done( final UUID uuid ) {
                if ( uuid != null ) {
                    Whitelist.getInstance().getProxy().getScheduler().runAsync( Whitelist.getInstance(), new Runnable() {
                        @Override
                        public void run() {
                            if ( !Whitelist.getInstance().getWhitelistEntityManager().isWhitelisted( uuid ) ) {
                                Whitelist.getInstance().getWhitelistEntityManager().addToWhitelist( uuid );
                            }

                            successCallback.done( true );
                        }
                    } );
                } else {
                    successCallback.done( false );
                }
            }
        } );
    }

    private void getUUIDForUser( final String user, final Callback<UUID> callback ) {
        ProxiedPlayer player = Whitelist.getInstance().getProxy().getPlayer( user );
        if ( player != null ) {
            callback.done( player.getUniqueId() );
        } else {
            Whitelist.getInstance().getProxy().getScheduler().runAsync( Whitelist.getInstance(), new Runnable() {
                @Override
                public void run() {
                    // Ask Mojang API for the UUID
                    HttpProfileRepository profileRepository = new HttpProfileRepository( "minecraft" );
                    Profile[] profiles = profileRepository.findProfilesByNames( user );
                    if ( profiles.length > 0 ) {
                        callback.done( getUUIDFromFlatString( profiles[0].getId() ) );
                    } else {
                        callback.done( null );
                    }
                }
            } );
        }
    }

    private UUID getUUIDFromFlatString( String uuid ) {
        return UUID.fromString( uuid.substring( 0, 8 ) + "-" + uuid.substring( 8, 12 ) + "-" + uuid.substring( 12, 16 ) + "-" + uuid.substring( 16, 20 ) + "-" + uuid.substring( 20 ) );
    }

    private void deleteFromWhitelist( String user, final Callback<Boolean> successCallback ) {
        getUUIDForUser( user, new Callback<UUID>() {
            @Override
            public void done( final UUID uuid ) {
                if ( uuid != null ) {
                    Whitelist.getInstance().getProxy().getScheduler().runAsync( Whitelist.getInstance(), new Runnable() {
                        @Override
                        public void run() {
                            if ( Whitelist.getInstance().getWhitelistEntityManager().isWhitelisted( uuid ) ) {
                                Whitelist.getInstance().getWhitelistEntityManager().deleteFromWhitelist( uuid );

                                ProxiedPlayer player = Whitelist.getInstance().getProxy().getPlayer( uuid );
                                if ( player != null ) {
                                    player.disconnect( "Dir wurde der Zugang zu diesem Server entzogen" );
                                }
                            }

                            successCallback.done( true );
                        }
                    } );
                } else {
                    successCallback.done( false );
                }
            }
        } );
    }

    private void checkWhitelisted( String user, final Callback<Boolean> successCallback ) {
        getUUIDForUser( user, new Callback<UUID>() {
            @Override
            public void done( final UUID uuid ) {
                if ( uuid != null ) {
                    Whitelist.getInstance().getProxy().getScheduler().runAsync( Whitelist.getInstance(), new Runnable() {
                        @Override
                        public void run() {
                            successCallback.done( Whitelist.getInstance().getWhitelistEntityManager().isWhitelisted( uuid ) );
                        }
                    } );
                } else {
                    successCallback.done( false );
                }
            }
        } );
    }
}
