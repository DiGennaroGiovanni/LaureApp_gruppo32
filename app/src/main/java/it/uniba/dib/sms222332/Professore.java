package it.uniba.dib.sms222332;

public class Professore {


    public String getNome() {
        return nome;
    }



    public String getCognome() {
        return cognome;
    }



    public String getRuolo() {
        return ruolo;
    }

    public String getEmail() {
        return email;
    }



    private String nome;
    private String cognome;
    private String ruolo;
    private String email;



    public Professore(String nome, String cognome, String ruolo, String email) {
        this.nome = nome;
        this.cognome = cognome;
        this.ruolo = ruolo;
        this.email = email;
    }




}
