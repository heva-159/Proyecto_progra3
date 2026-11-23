package pe.edu.pucp.tutorpucp.tutorpucprest.DTO;

import java.util.ArrayList;
import java.util.List;

public class ReporteTutorDTO {

    private int totalDictadas;
    private Double calificacionPromedio;
    private List<SesionReporteItemDTO> sesiones = new ArrayList<>();

    public ReporteTutorDTO() {}
    public ReporteTutorDTO(int totalDictadas, Double calificacionPromedio, List<SesionReporteItemDTO> sesiones) {
        this.totalDictadas = totalDictadas;
        this.calificacionPromedio = calificacionPromedio;
        this.sesiones = sesiones;
    }
    public int getTotalDictadas() { return totalDictadas; }
    public void setTotalDictadas(int totalDictadas) { this.totalDictadas = totalDictadas; }
    public Double getCalificacionPromedio() { return calificacionPromedio; }
    public void setCalificacionPromedio(Double calificacionPromedio) { this.calificacionPromedio = calificacionPromedio; }
    public List<SesionReporteItemDTO> getSesiones() { return sesiones; }
    public void setSesiones(List<SesionReporteItemDTO> sesiones) { this.sesiones = sesiones; }


}
