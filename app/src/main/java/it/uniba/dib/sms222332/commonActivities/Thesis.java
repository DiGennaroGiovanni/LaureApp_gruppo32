package it.uniba.dib.sms222332.commonActivities;

import java.util.Objects;

public class Thesis {

    public Thesis(String name, String professor) {
        this.name = name;
        this.professor = professor;
    }

    private final String name;
    private String professor;

    public String getName() {
        return name;
    }

    public String getProfessor() {
        return professor;
    }

    public void setProfessor(String professor){
        this.professor = professor;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        Thesis thesis = (Thesis) o;
        return name.equals(thesis.name) && professor.equals(thesis.professor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, professor);
    }
}
