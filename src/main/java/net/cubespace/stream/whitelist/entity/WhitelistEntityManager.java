package net.cubespace.stream.whitelist.entity;

import net.cubespace.stream.whitelist.Whitelist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Created by Fabian on 24.06.15.
 */
public class WhitelistEntityManager {
    public boolean isWhitelisted( UUID uuid ) {
        Connection connection = null;

        try {
            connection = Whitelist.getInstance().getMysqlConnection().getConnection();

            PreparedStatement preparedStatement = connection.prepareStatement( "SELECT `id` FROM `whitelist` WHERE `uuid` = ?;" );
            preparedStatement.setObject( 1, uuid.toString().replaceAll( "-", "" ) );

            if ( preparedStatement.execute() ) {
                ResultSet resultSet = preparedStatement.getResultSet();
                if ( resultSet.next() ) {
                    return true;
                }
            }
        } catch ( SQLException e ) {
            e.printStackTrace();
        } finally {
            if ( connection != null ) {
                try {
                    connection.close();
                } catch ( SQLException e ) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    public boolean addToWhitelist( UUID uuid ) {
        Connection connection = null;

        try {
            connection = Whitelist.getInstance().getMysqlConnection().getConnection();

            PreparedStatement preparedStatement = connection.prepareStatement( "INSERT INTO `whitelist`(`uuid`) VALUES(?);" );
            preparedStatement.setObject( 1, uuid.toString().replaceAll( "-", "" ) );

            if ( preparedStatement.execute() ) {
                return true;
            }
        } catch ( SQLException e ) {
            e.printStackTrace();
        } finally {
            if ( connection != null ) {
                try {
                    connection.close();
                } catch ( SQLException e ) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    public boolean deleteFromWhitelist( UUID uuid ) {
        Connection connection = null;

        try {
            connection = Whitelist.getInstance().getMysqlConnection().getConnection();

            PreparedStatement preparedStatement = connection.prepareStatement( "DELETE FROM `whitelist` WHERE `uuid` = ?;" );
            preparedStatement.setObject( 1, uuid.toString().replaceAll( "-", "" ) );

            if ( preparedStatement.execute() ) {
                return true;
            }
        } catch ( SQLException e ) {
            e.printStackTrace();
        } finally {
            if ( connection != null ) {
                try {
                    connection.close();
                } catch ( SQLException e ) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }
}
