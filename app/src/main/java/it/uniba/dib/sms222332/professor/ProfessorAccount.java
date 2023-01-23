package it.uniba.dib.sms222332.professor;

import it.uniba.dib.sms222332.commonActivities.Account;

public class ProfessorAccount implements Account {

    private String name;
    private String surname;
    private String faculty;
    private String email;
    private final String accountType = "Professor";

    public ProfessorAccount(String name, String surname, String faculty, String email) {
        this.name = name;
        this.surname = surname;
        this.faculty = faculty;
        this.email = email;
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSurname() {
        return surname;
    }

    @Override
    public String getRequest() {
        return null;
    }

    @Override
    public String getBadgeNumber() {
        return null;
    }

    @Override
    public String getAccountType() {
        return accountType;
    }

    @Override
    public String getFaculty() {
        return faculty;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void setRequest(String request){

    }












}
