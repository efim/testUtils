/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.util.stream;

import java.util.Comparator;

/**
 *
 * @author enefedov
 */
public class Employee implements Comparable<Employee>, Cloneable {

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the age
     */
    public int getAge() {
        return age;
    }

    /**
     * @return if is male
     */
    public boolean isMale() {
        return male;
    }

    /**
     * @return the salary
     */
    public float getSalary() {
        return salary;
    }

    /**
     * @return the title
     */
    public Title getTitle() {
        return title;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @param age the age to set
     */
    public void setAge(int age) {
        this.age = age;
    }

    /**
     * @param male the male to set
     */
    public void setMale(boolean male) {
        this.male = male;
    }

    /**
     * @param salary the salary to set
     */
    public void setSalary(float salary) {
        this.salary = salary;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(Title title) {
        this.title = title;
    }

    static enum Title {
        IC1, IC2, IC3, IC4
    }

    static enum Rule {

        ID {
                    @Override
                    String getValue(Employee e) {
                        return e.getId();
                    }

                    @Override
                    Comparator<Employee> getComparator() {
                        return (o1, o2) -> o1.getId().compareTo(o2.getId());
                    }
                },
        AGE {
                    @Override
                    Integer getValue(Employee e) {
                        return e.getAge();
                    }

                    @Override
                    Comparator<Employee> getComparator() {
                        return (o1, o2) -> Integer.valueOf(o1.getAge()).compareTo(o2.getAge());
                    }
                },
        MALE {
                    @Override
                    Boolean getValue(Employee e) {
                        return e.isMale();
                    }

                    @Override
                    Comparator<Employee> getComparator() {
                        return (o1, o2) -> Boolean.valueOf(o1.isMale()).compareTo(o2.isMale());
                    }
                },
        SALARY {
                    @Override
                    Float getValue(Employee e) {
                        return e.getSalary();
                    }

                    @Override
                    Comparator<Employee> getComparator() {
                        return (o1, o2) -> Float.valueOf(o1.getSalary()).compareTo(o2.getSalary());
                    }
                },
        TITLE {
                    @Override
                    Title getValue(Employee e) {
                        return e.getTitle();
                    }

                    @Override
                    Comparator<Employee> getComparator() {
                        return (o1, o2) -> Integer.valueOf(o1.getTitle().ordinal()).compareTo(o2.getTitle().ordinal());
                    }
                };

        abstract Comparator<Employee> getComparator();

        abstract Object getValue(Employee e);
    }

    private String id = null;
    private int age = 0;
    private boolean male = false;
    private float salary = 0;
    private Title title = null;
    static final int MIN_ID = 5;
    static final int MAX_ID = 10;
    static final int MIN_AGE = 18;
    static final int MAX_AGE = 65;
    static final int MIN_SALARY = 1000;
    static final int MAX_SALARY = 100000;

    @Override
    public boolean equals(Object other) {
        if (other instanceof Employee) {
            return this.getId().equals(((Employee) other).getId());
        } else {
            return false;
        }
    }

    @Override
    public int compareTo(Employee t) {
        return this.getId().compareTo(t.getId());
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }

    @Override
    public Employee clone() {
        Employee e = new Employee();
        e.setAge(this.getAge());
        e.setId(this.getId());
        e.setMale(this.isMale());
        e.setSalary(this.getSalary());
        e.setTitle(this.getTitle());
        return e;
    }

    @Override
    public String toString() {
        return "id=" + getId() + ", age=" + getAge() + ". male=" + isMale() + ", salary=" + getSalary() + ", title=" + getTitle();
    }
}
