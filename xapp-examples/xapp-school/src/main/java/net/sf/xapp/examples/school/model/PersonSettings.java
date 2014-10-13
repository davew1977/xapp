package net.sf.xapp.examples.school.model;

/**
 * © Webatron Ltd
 * Created by dwebber
 */
public class PersonSettings {
    private Hat favouriteHat;
    private String[] favouriteWords;

    public Hat getFavouriteHat() {
        return favouriteHat;
    }

    public void setFavouriteHat(Hat favouriteHat) {
        this.favouriteHat = favouriteHat;
    }

    public String[] getFavouriteWords() {
        return favouriteWords;
    }

    public void setFavouriteWords(String[] favouriteWords) {
        this.favouriteWords = favouriteWords;
    }
}
