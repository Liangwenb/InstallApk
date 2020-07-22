package sample;

public class Device {
    public String product;
    public String model;
    public String device;
    public String id;


    @Override
    public String toString() {
        return "Device{" +
                "product='" + product + '\'' +
                ", model='" + model + '\'' +
                ", device='" + device + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
