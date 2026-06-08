import java.math.BigDecimal;
import java.util.UUID;

public class Ingredient extends EntityBase<UUID> {

    protected String name;
    protected BigDecimal cost;

    protected Ingredient(UUID id, String name, BigDecimal cost) {
        super(id);
        this.name = name;
        this.cost = cost;
    }

    public static Ingredient create(UUID id,String name, BigDecimal cost) {
        return new Ingredient(id, name, cost);
    }
   
    public void UpdateCost(BigDecimal cost){
        //ingredient:cost_update
        this.cost = cost;
    }
    public String getName() {
        return name;
    }

    public BigDecimal getCost() {
        return cost;
    }
}
