package pe.edu.pucp.tutorpucp.tutorpucprest.DTO;

import java.math.BigDecimal;

public class GuardarTarifaRequest {

    private int idTutor;
    private int idCurso;
    private BigDecimal montoPorHora;

    public GuardarTarifaRequest() {}


    public int getIdTutor() { return idTutor; }
    public void setIdTutor(int idTutor) { this.idTutor = idTutor; }
    public int getIdCurso() { return idCurso; }
    public void setIdCurso(int idCurso) { this.idCurso = idCurso; }
    public BigDecimal getMontoPorHora() { return montoPorHora; }
    public void setMontoPorHora(BigDecimal montoPorHora) { this.montoPorHora = montoPorHora; }


}
