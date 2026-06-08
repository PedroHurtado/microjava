import java.time.Instant;
import java.util.UUID;

public abstract class DomainEvent extends EntityBase<UUID> {

    protected final Instant occurredOn;

    protected DomainEvent(UUID id, Instant occurredOn) {
        super(id);
        this.occurredOn = occurredOn;
    }

    public Instant getOccurredOn() {
        return occurredOn;
    }
}
