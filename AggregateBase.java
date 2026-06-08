import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AggregateBase<ID> extends EntityBase<ID> {

    protected final List<DomainEvent> events;

    protected AggregateBase(ID id) {
        super(id);
        this.events = new ArrayList<>();
    }

    public void add(DomainEvent event) {
        events.add(event);
    }

    public void remove(DomainEvent event) {
        events.remove(event);
    }

    public void clear() {
        events.clear();
    }

    public List<DomainEvent> getEvents() {
        return Collections.unmodifiableList(new ArrayList<>(events));
    }
}
