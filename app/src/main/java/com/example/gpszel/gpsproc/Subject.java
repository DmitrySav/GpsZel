package com.example.gpszel.gpsproc;

import com.example.gpszel.gpsproc.Observer;

/**
 * Created by А on 25.08.2016.
 */

public interface Subject {
    void registerObserver(Observer o);
    void removeObserver(Observer o);
    void notifyObservers();
}
