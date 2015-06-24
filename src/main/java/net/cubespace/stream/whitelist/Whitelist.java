package net.cubespace.stream.whitelist;

import lombok.Getter;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.cubespace.stream.whitelist.config.MySQLConfig;
import net.cubespace.stream.whitelist.entity.WhitelistEntityManager;
import net.cubespace.stream.whitelist.listener.PlayerLoginListener;
import net.md_5.bungee.api.plugin.Plugin;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by Fabian on 24.06.15.
 */
public class Whitelist extends Plugin {
    @Getter private static Whitelist instance;

    @Getter private BasicDataSource mysqlConnection;
    @Getter private WhitelistEntityManager whitelistEntityManager;

    @Override
    public void onEnable() {
        instance = this;

        // Test to load
        try {
            Whitelist.class.getClassLoader().loadClass( "org.apache.commons.pool2.impl.DefaultEvictionPolicy" );
        } catch ( ClassNotFoundException e ) {
            e.printStackTrace();
        }

        // Read or create Configuration File
        MySQLConfig mySQLConfig = new MySQLConfig();
        try {
            mySQLConfig.init();
        } catch ( InvalidConfigurationException e ) {
            e.printStackTrace();
        }

        // Create database Pool
        createDatabasePool( mySQLConfig );

        // Create entity manager
        whitelistEntityManager = new WhitelistEntityManager();

        // Register listener
        getProxy().getPluginManager().registerListener( this, new PlayerLoginListener() );
    }

    private void createDatabasePool( MySQLConfig mySQLConfig ) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader( Whitelist.class.getClassLoader() );

        mysqlConnection = new BasicDataSource();

        // Init basic Driver settings
        mysqlConnection.setDriverClassName( "com.mysql.jdbc.Driver" );
        mysqlConnection.setUrl( "jdbc:mysql://" + mySQLConfig.getHost() + ":3306/" + mySQLConfig.getDatabase() );
        mysqlConnection.setUsername( mySQLConfig.getUser() );
        mysqlConnection.setPassword( mySQLConfig.getPassword() );

        // Setup connection pooling
        mysqlConnection.setMaxIdle( mySQLConfig.getPoolSize() );
        mysqlConnection.setMinIdle( 1 );
        mysqlConnection.setDriverClassLoader( Whitelist.class.getClassLoader() );

        try {
            Connection connection = mysqlConnection.getConnection();
            connection.close();
        } catch ( SQLException e ) {
            e.printStackTrace();
            getProxy().stop();
        }

        Thread.currentThread().setContextClassLoader( classLoader );
    }
}
