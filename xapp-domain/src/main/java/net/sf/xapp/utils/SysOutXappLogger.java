package net.sf.xapp.utils;

/**
 */
public class SysOutXappLogger implements XappLogger {
    @Override
    public void debug(String message) {
        System.out.println(message);
    }

    @Override
    public void info(String message) {
        System.out.println(message);

    }

    @Override
    public void error(String message) {
        System.out.println(message);
    }
}
