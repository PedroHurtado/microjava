import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Pizza extends AggregateBase<UUID> {

    private static final BigDecimal PROFIT = new BigDecimal("1.20");

    protected String name;
    protected String description;
    protected String url;    
    protected final Set<Ingredient> ingredients;

    protected Pizza(UUID id, String name, String description, String url,
                     Set<Ingredient> ingredients) {
        super(id);
        this.name = name;
        this.description = description;
        this.url = url;        
        this.ingredients = new HashSet<>(ingredients);
    }

    public static Pizza create(UUID id,String name, String description, String url,Set<Ingredient> ingredients) {
        //pizza:create        
        return new Pizza(id, name, description, url,
                 ingredients);
    }

   
    public void addIngredient(Ingredient ingredient) {
        ingredients.add(ingredient);
        //pizza:addingredient

        
    }

    public void removeIngredient(Ingredient ingredient) {
        ingredients.remove(ingredient);
        //pizza:remoeingredient
    }

    public BigDecimal getPrice() {
        var result =  ingredients.stream()
                .map(Ingredient::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return result.multiply(PROFIT);        
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }  

    public Set<Ingredient> getIngredients() {
        return Collections.unmodifiableSet(ingredients);
    }
}
