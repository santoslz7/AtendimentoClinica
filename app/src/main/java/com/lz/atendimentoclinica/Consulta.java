package com.lz.atendimentoclinica;

public class Consulta {
    private String id;
    private String pacienteId;
    private String clinicaId;
    private String medicoId;
    private String data;
    private String hora;
    private String nomePaciente;
    private String nomeMedico;
    private String especialidade; // CORRIGIDO: era "getEspecialidade"
    private String status;

    public Consulta() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPacienteId() { return pacienteId; }
    public void setPacienteId(String pacienteId) { this.pacienteId = pacienteId; }

    public String getClinicaId() { return clinicaId; }
    public void setClinicaId(String clinicaId) { this.clinicaId = clinicaId; }

    public String getMedicoId() { return medicoId; }
    public void setMedicoId(String medicoId) { this.medicoId = medicoId; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }

    public String getNomePaciente() { return nomePaciente; }
    public void setNomePaciente(String nomePaciente) { this.nomePaciente = nomePaciente; }

    public String getNomeMedico() { return nomeMedico; }
    public void setNomeMedico(String nomeMedico) { this.nomeMedico = nomeMedico; }

    public String getEspecialidade() { return especialidade; }
    public void setEspecialidade(String especialidade) { this.especialidade = especialidade; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}