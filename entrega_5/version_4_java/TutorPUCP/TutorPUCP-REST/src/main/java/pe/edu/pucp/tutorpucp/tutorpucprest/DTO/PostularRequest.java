package pe.edu.pucp.tutorpucp.tutorpucprest.DTO;


public class PostularRequest {
    private int idTutor;
    private int idCurso;
    private String archivoCertificadoUrl;
    private String comentarios;
    // El PDF del certificado en Base64 (JSON solo lleva texto). Si llega, Java guarda el archivo de verdad.
    private String certificadoBase64;

    public PostularRequest() {}

    public int getIdTutor() { return idTutor; }
    public void setIdTutor(int idTutor) { this.idTutor = idTutor; }

    public int getIdCurso() { return idCurso; }
    public void setIdCurso(int idCurso) { this.idCurso = idCurso; }

    public String getArchivoCertificadoUrl() { return archivoCertificadoUrl; }
    public void setArchivoCertificadoUrl(String archivoCertificadoUrl) { this.archivoCertificadoUrl = archivoCertificadoUrl; }

    public String getComentarios() { return comentarios; }
    public void setComentarios(String comentarios) { this.comentarios = comentarios; }

    public String getCertificadoBase64() { return certificadoBase64; }
    public void setCertificadoBase64(String certificadoBase64) { this.certificadoBase64 = certificadoBase64; }
}
