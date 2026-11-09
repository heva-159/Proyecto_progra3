using ClosedXML.Excel;
using Microsoft.JSInterop;
using QuestPDF.Fluent;
using QuestPDF.Helpers;
using QuestPDF.Infrastructure;

namespace TutorPUCP.Servicios;

/// <summary>
/// Arma un reporte en PDF (QuestPDF) o Excel (ClosedXML) con lo que se ve en pantalla
/// y lo descarga en el navegador. No consulta ninguna base de datos.
/// </summary>
public static class Exportador
{
    public record Tabla(string Titulo, string[] Columnas, IEnumerable<string[]> Filas);

    static Exportador() => QuestPDF.Settings.License = LicenseType.Community;

    public static async Task DescargarAsync(IJSRuntime js, string nombreBase, string formato, string titulo, string subtitulo, params Tabla[] tablas)
    {
        var pdf = formato == "PDF";
        var bytes = pdf ? Pdf(titulo, subtitulo, tablas) : Excel(titulo, subtitulo, tablas);
        var nombre = nombreBase + (pdf ? ".pdf" : ".xlsx");
        var tipo = pdf ? "application/pdf" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        await js.InvokeVoidAsync("tp.descargar", nombre, tipo, Convert.ToBase64String(bytes));
    }

    private static byte[] Pdf(string titulo, string subtitulo, Tabla[] tablas) =>
        Document.Create(doc => doc.Page(page =>
        {
            page.Size(PageSizes.A4.Landscape());
            page.Margin(32);
            page.DefaultTextStyle(t => t.FontSize(10).FontColor("#1e293b"));

            page.Header().PaddingBottom(12).Column(col =>
            {
                col.Item().Text("TutorPUCP").FontSize(11).Bold().FontColor("#2563eb");
                col.Item().Text(titulo).FontSize(20).Bold();
                col.Item().Text(subtitulo).FontColor("#64748b");
            });

            page.Content().Column(col =>
            {
                foreach (var tabla in tablas)
                {
                    col.Item().PaddingTop(10).PaddingBottom(6).Text(tabla.Titulo).FontSize(13).Bold().FontColor("#1e3a8a");
                    col.Item().Table(t =>
                    {
                        t.ColumnsDefinition(c =>
                        {
                            foreach (var _ in tabla.Columnas) c.RelativeColumn();
                        });

                        t.Header(h =>
                        {
                            foreach (var c in tabla.Columnas)
                                h.Cell().Background("#eef2ff").Padding(6).Text(c).Bold().FontColor("#1e3a8a");
                        });

                        var filas = tabla.Filas.ToList();
                        if (filas.Count == 0)
                        {
                            t.Cell().ColumnSpan((uint)tabla.Columnas.Length).Padding(6).Text("Sin registros en este rango.").FontColor("#64748b");
                        }

                        foreach (var fila in filas)
                            foreach (var celda in fila)
                                t.Cell().BorderBottom(0.5f).BorderColor("#e5eaf3").Padding(6).Text(celda);
                    });
                }
            });

            page.Footer().AlignRight().Text(t =>
            {
                t.Span($"Generado el {DateTime.Now:dd/MM/yyyy HH:mm} · Página ").FontColor("#94a3b8").FontSize(8);
                t.CurrentPageNumber().FontColor("#94a3b8").FontSize(8);
            });
        })).GeneratePdf();

    private static byte[] Excel(string titulo, string subtitulo, Tabla[] tablas)
    {
        using var libro = new XLWorkbook();

        foreach (var tabla in tablas)
        {
            var hoja = libro.Worksheets.Add(tabla.Titulo.Length > 31 ? tabla.Titulo[..31] : tabla.Titulo);
            hoja.Cell(1, 1).Value = titulo;
            hoja.Cell(1, 1).Style.Font.SetBold().Font.SetFontSize(16).Font.SetFontColor(XLColor.FromHtml("#1e3a8a"));
            hoja.Cell(2, 1).Value = subtitulo;
            hoja.Cell(2, 1).Style.Font.SetFontColor(XLColor.FromHtml("#64748b"));

            for (var c = 0; c < tabla.Columnas.Length; c++)
            {
                var celda = hoja.Cell(4, c + 1);
                celda.Value = tabla.Columnas[c];
                celda.Style.Font.SetBold().Fill.SetBackgroundColor(XLColor.FromHtml("#eef2ff")).Font.SetFontColor(XLColor.FromHtml("#1e3a8a"));
                hoja.Column(c + 1).Width = 22;
            }

            var f = 5;
            foreach (var fila in tabla.Filas)
            {
                for (var c = 0; c < fila.Length; c++) hoja.Cell(f, c + 1).Value = fila[c];
                f++;
            }

            if (f > 5) hoja.Range(4, 1, f - 1, tabla.Columnas.Length).SetAutoFilter();
        }

        using var ms = new MemoryStream();
        libro.SaveAs(ms);
        return ms.ToArray();
    }
}
