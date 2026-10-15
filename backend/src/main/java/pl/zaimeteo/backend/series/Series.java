package pl.zaimeteo.backend.series;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "series")
public class Series {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Double minValue;
    private Double maxValue;
    private String color;
    private String icon;
    private String unit;

    public Long getId() { return id; }
    public String getName() { return name; }
    public Double getMinValue() { return minValue; }
    public Double getMaxValue() { return maxValue; }
    public String getColor() { return color; }
    public String getIcon() { return icon; }
    public String getUnit() { return unit; }
 
    public void setName(String name) { this.name = name; }
    public void setMinValue(Double minValue) { this.minValue = minValue; }
    public void setMaxValue(Double maxValue) { this.maxValue = maxValue; }
    public void setColor(String color) { this.color = color; }
    public void setIcon(String icon) { this.icon = icon; }
    public void setUnit(String unit) { this.unit = unit; }
}