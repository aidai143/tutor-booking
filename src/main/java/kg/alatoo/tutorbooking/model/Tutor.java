package kg.alatoo.tutorbooking.model;

import java.util.Objects;

public class Tutor {
    private Integer id;
    private String name;
    private String phone;
    private int hourlyRateSom;

    public Tutor(String name, String phone, int hourlyRateSom) {
        setName(name);
        setPhone(phone);
        setHourlyRateSom(hourlyRateSom);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public int getHourlyRateSom() {
        return hourlyRateSom;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Имя репетитора не может быть пустым");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Имя слишком длинное (макс. 100 символов)");
        }
        this.name = name.trim();
    }

    public void setPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Телефон не может быть пустым");
        }
        if (phone.length() > 20) {
            throw new IllegalArgumentException("Телефон слишком длинный (макс. 20 символов)");
        }
        this.phone = phone.trim();
    }

    public void setHourlyRateSom(int hourlyRateSom) {
        if (hourlyRateSom <= 0) {
            throw new IllegalArgumentException("Ставка должна быть больше 0 сом");
        }
        this.hourlyRateSom = hourlyRateSom;
    }

    @Override
    public String toString() {
        return name + " (" + hourlyRateSom + " сом/час)";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Tutor other = (Tutor) obj;
        return Objects.equals(phone, other.phone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phone);
    }
}
