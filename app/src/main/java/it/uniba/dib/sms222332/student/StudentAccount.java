package it.uniba.dib.sms222332.student;

import it.uniba.dib.sms222332.commonActivities.Account;

public class StudentAccount implements Account {


    private String name;
    private String surname;
    private String badgeNumber;
    private String faculty;
    private String email;
    private final String accountType = "Student";


    public StudentAccount(String name, String surname, String badgeNumber, String faculty, String email) {
        this.name = name;
        this.surname = surname;
        this.badgeNumber = badgeNumber;
        this.faculty = faculty;
        this.email = email;
    }

    @Override
    public String getEmail() {
        return email;
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
    public String getBadgeNumber() {
        return badgeNumber;
    }

    @Override
    public String getAccountType() {
        return accountType;
    }

    @Override
    public String getFaculty() {
        return faculty;
    }


}
