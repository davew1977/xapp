/*
 *
 *
 * 1.1.3
 * Author: davidw
 *
 */
package net.sf.xapp.net.client.tools.adminclient;

import net.sf.xapp.annotations.objectmodelling.Reference;

/**
 * "bean" for storing the settings
 */
public class TestParams
{
    @Reference(tooltipMethod = "tooltip")
    public SpringConfig m_springConfig;
}