package net.sf.xapp.examples.school;

import org.junit.Test;

/**
 * © Webatron Ltd
 * Created by dwebber
 */
public class AutoTest extends TestBase {

    /**
     * scenario 1 (check basic start up):
     * 1) Start client
     * 2) ensure entire state is loaded
     * 3) ensure rev.txt contains 0
     * 4) ensure obj.xml is written
     * 5) do model change (add text file to a pupil)
     * 6) ensure deltas.xml is written
     *
     */
    @Test
    public void testScenario1() {


        //TODO ensure example school s1 exists

        //TODO join 2 clients

        //TODO scenario 1: 1 client check that entire state is returned

        //TODO scenario 2:
        //TODO 1) 1 client start clean
        //TOD, make edit (add text file), force close
    }

}
