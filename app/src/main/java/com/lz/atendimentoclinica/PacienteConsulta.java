package com.lz.atendimentoclinica;

public class PacienteConsulta {
    public String nomePaciente;
    public String nomeMedico;
    public String especialidade;
    public String data;
    public String horario;
    public String status;

    public PacienteConsulta(String nomePaciente, String nomeMedico,
                            String especialidade, String data,
                            String horario, String status) {
        this.nomePaciente  = nomePaciente;
        this.nomeMedico    = nomeMedico;
        this.especialidade = especialidade;
        this.data          = data;
        this.horario       = horario;
        this.status        = status;
    }
}