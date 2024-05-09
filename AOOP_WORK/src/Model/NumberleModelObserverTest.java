package Model;

import java.util.Observable;
import java.util.Observer;
import java.util.ArrayList;
import java.util.List;

class NumberleModelObserverTest implements Observer {
    List<Object> notifications = new ArrayList<>();

    @Override
    public void update(Observable o, Object arg) {
        notifications.add(arg);
    }

    public List<Object> getNotifications() {
        return notifications;
    }
}

