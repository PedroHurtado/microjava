import java.util.Objects;

public abstract class EntityBase<ID> {

    protected final ID id;

    protected EntityBase(ID id) {
        this.id = id;
    }

    public ID getId() {
        return id;
    }

    @Override
    public final boolean equals(Object obj) {
        return obj instanceof EntityBase<?> other
                && getClass() == other.getClass()
                && Objects.equals(id, other.id);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getClass(), id);
    }
}
