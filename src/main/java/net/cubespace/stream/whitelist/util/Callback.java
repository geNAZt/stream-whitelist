package net.cubespace.stream.whitelist.util;

/**
 * Created by Fabian on 24.06.15.
 */
public interface Callback<T> {
    void done( T doneValue );
}
