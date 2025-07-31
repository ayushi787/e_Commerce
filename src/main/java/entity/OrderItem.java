package entity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity

public class OrderItem {


    @Id

    @GeneratedValue

    private Long
            id;

    @ManyToOne

    private com.example.ecommerce.entity.Order
            order;

    @ManyToOne

    private com.example.ecommerce.entity.Product
            product;

    private int quantity;

    private String name;

    private double price;

    private String
            imageName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public com.example.ecommerce.entity.Order getOrder() {
        return order;
    }

    public void setOrder(com.example.ecommerce.entity.Order order) {
        this.order = order;
    }

    public com.example.ecommerce.entity.Product getProduct() {
        return product;
    }

    public void setProduct(com.example.ecommerce.entity.Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}