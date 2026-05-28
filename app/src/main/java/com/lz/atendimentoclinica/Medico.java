package com.lz.atendimentoclinica;

public class Medico {
    private String id; // ADICIONADO: Necessário para o m.getId() funcionar
    private String nome;
    private String crm;
    private String especialidade;
    private String clinicaId;

    public Medico() {}

    // Getter e Setter para o ID
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCrm() { return crm; }
    public void setCrm(String crm) { this.crm = crm; }

    public String getEspecialidade() { return especialidade; }
    public void setEspecialidade(String especialidade) { this.especialidade = especialidade; }

    public String getClinicaId() { return clinicaId; }
    public void setClinicaId(String clinicaId) { this.clinicaId = clinicaId; }
}