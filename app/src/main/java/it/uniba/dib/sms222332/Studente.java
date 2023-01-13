package it.uniba.dib.sms222332;

public class Studente {

    public Studente(String nome, String cognome, String matricola, String facolta, String email) {
        this.nome = nome;
        this.cognome = cognome;
        this.matricola = matricola;
        this.facolta = facolta;
        this.email = email;
    }


    private String nome;
    private String cognome;
    private String matricola;
    private String facolta;
    private String email;


    public String getEmail() {
        return email;
    }


    public String getNome() {
        return nome;
    }


    public String getCognome() {
        return cognome;
    }


    public String getMatricola() {
        return matricola;
    }


    public String getFacolta() {
        return facolta;
    }


}
