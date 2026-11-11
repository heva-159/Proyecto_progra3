package pe.edu.pucp.tutorpucp.tutorpucprest.DTO;

import pe.edu.pucp.tutorPUCP.modelo.tutoria.Tarifa;
import java.math.BigDecimal;

public class TarifaDTO {

    private int idTarifa;
    private int idCurso;
    private String cursoCodigo;
    private String cursoNombre;
    private BigDecimal montoPorHora;

    public TarifaDTO() {}

    public TarifaDTO(Tarifa tarifa) {
        this.idTarifa = tarifa.getIdTarifa();
        if (tarifa.getCurso() != null) {
            this.idCurso = tarifa.getCurso().getIdCurso();
            this.cursoCodigo = tarifa.getCurso().getCodigo();
            this.cursoNombre = tarifa.getCurso().getNombre();
        }
        this.montoPorHora = tarifa.getMontoPorHora();
    }

    public int getIdTarifa() { return idTarifa; }
    public void setIdTarifa(int idTarifa) { this.idTarifa = idTarifa; }
    public int getIdCurso() { return idCurso; }
    public void setIdCurso(int idCurso) { this.idCurso = idCurso; }
    public String getCursoCodigo() { return cursoCodigo; }
    public void setCursoCodigo(String cursoCodigo) { this.cursoCodigo = cursoCodigo; }
    public String getCursoNombre() { return cursoNombre; }
    public void setCursoNombre(String cursoNombre) { this.cursoNombre = cursoNombre; }
    public BigDecimal getMontoPorHora() { return montoPorHora; }
    public void setMontoPorHora(BigDecimal montoPorHora) { this.montoPorHora = montoPorHora; }


}
