package huylvph30524.poly.lab2.Modal;

public class Item {
    private String id;
    private String name;
    private String price;
    private String brand;

    public Item(String id, String name, int price, String brand) {
        this.id = id;
        this.name = name;
        this.price = String.valueOf(price);
        this.brand = brand;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }
}
