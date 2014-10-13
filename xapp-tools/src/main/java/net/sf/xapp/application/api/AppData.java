package net.sf.xapp.application.api;

/**
 * © Webatron Ltd
 * Created by dwebber
 */
public class AppData {

    private String lastSelected;
    private int dividerLocation;

    public String getLastSelected()
    {
        return lastSelected;
    }

    public void setLastSelected(String lastEdited)
    {
        lastSelected = lastEdited;
    }

    public void setDividerLocation(int dividerLocation)
    {
        this.dividerLocation = dividerLocation;
    }

    public int getDividerLocation()
    {
        return dividerLocation;
    }
}
