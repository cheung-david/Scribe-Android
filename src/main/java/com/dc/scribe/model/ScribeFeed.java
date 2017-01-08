package com.dc.scribe.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by david on 18/08/16.
 */
public class ScribeFeed implements Serializable {
    ArrayList<ScribeCard> scribeCards;

    public ScribeFeed(ArrayList<ScribeCard> scribeCards) {
        this.scribeCards = scribeCards;
    }

    public ArrayList<ScribeCard> getScribeCards() {
        return scribeCards;
    }

    public void setScribeCards(ArrayList<ScribeCard> scribeCards) {
        this.scribeCards = scribeCards;
    }
}
