package kg.alatoo.tutorbooking.model;

import java.util.Objects;

public class Student {
    private Integer id;
    private String name;
    private String phone;
    private int grade;

    public Student(String name, String phone, int grade) {
        setName(name);
        setPhone(phone);
        setGrade(grade);
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

    public int getGrade() {
        return grade;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Имя не может быть пустым");
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
        this.phone = phone.trim();
    }

    public void setGrade(int grade){
        if (grade < 1 || grade > 11) {

            throw new IllegalArgumentException("Класс должен быть от 1 до 11");
        }
        this.grade = grade;
    }

    @Override
    public String toString() {
        return name + " (" + grade + " класс), " + phone;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Student other = (Student) obj;
        return Objects.equals(phone, other.phone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phone);
    }
}



