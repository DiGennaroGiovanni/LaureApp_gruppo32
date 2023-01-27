package it.uniba.dib.sms222332.commonActivities;

public class Thesis {

    public Thesis(String name, String professor) {
        this.name = name;
        this.professor = professor;
    }

    private final String name;
    private final String professor;


    public String getName() {
        return name;
    }

    public String getProfessor() {
        return professor;
    }
}
