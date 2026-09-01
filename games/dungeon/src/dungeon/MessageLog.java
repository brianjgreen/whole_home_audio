package dungeon;

import java.util.ArrayList;
import java.util.List;

public class MessageLog {

    private static final int MAX_MESSAGES = 50;
    private final List<String> messages = new ArrayList<>();

    public void add(String message) {
        messages.add(message);
        if (messages.size() > MAX_MESSAGES) {
            messages.removeFirst();
        }
    }

    public List<String> recent(int count) {
        int start = Math.max(0, messages.size() - count);
        return messages.subList(start, messages.size());
    }

    public void clear() {
        messages.clear();
    }
}
