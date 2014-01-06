package net.sf.xapp.utils;

/**
 * collects log output from various utils
 */
public interface XappLogger {
    void debug(String message);
    void info(String message);
    void error(String message);
}
