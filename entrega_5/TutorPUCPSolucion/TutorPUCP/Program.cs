using Radzen;
using TutorPUCP.Components;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
// Español de Perú para fechas y números (RNF006).
var culturaPeru = new System.Globalization.CultureInfo("es-PE");
System.Globalization.CultureInfo.DefaultThreadCurrentCulture = culturaPeru;
System.Globalization.CultureInfo.DefaultThreadCurrentUICulture = culturaPeru;

builder.Services.AddBlazorBootstrap();
builder.Services.AddRadzenComponents();
builder.Services.AddScoped<TutorPUCP.Servicios.RolActual>();

builder.Services.AddHttpClient("api", cliente =>
{
    cliente.BaseAddress = new Uri(builder.Configuration["ApiRest:BaseUrl"]!);
});
builder.Services.AddScoped<TutorPUCP.Servicios.UsuariosApi>();
builder.Services.AddScoped<TutorPUCP.Servicios.MensajesApi>();
builder.Services.AddScoped<TutorPUCP.Servicios.ReclamosApi>();
builder.Services.AddScoped<TutorPUCP.Servicios.AcademicoApi>();

// Cliente REST del administrador (RF005-RF007). Usa la misma dirección: ApiRest:BaseUrl.
builder.Services.AddHttpClient<TutorPUCP.Servicios.AcademicoRestService>(cliente =>
{
    cliente.BaseAddress = new Uri(builder.Configuration["ApiRest:BaseUrl"]!);
    cliente.Timeout = TimeSpan.FromSeconds(60);
    cliente.DefaultRequestHeaders.Accept.ParseAdd("application/json");
});

// Cliente REST de sesiones (RF011-RF014) y de los reportes en PDF (RF017, RF018). Usa la misma dirección: ApiRest:BaseUrl.
builder.Services.AddHttpClient<TutorPUCP.Servicios.SesionRestService>(cliente =>
{
    cliente.BaseAddress = new Uri(builder.Configuration["ApiRest:BaseUrl"]!);
    cliente.Timeout = TimeSpan.FromSeconds(60);
    cliente.DefaultRequestHeaders.Accept.ParseAdd("application/json");
    cliente.DefaultRequestHeaders.Accept.ParseAdd("application/pdf");
});

builder.Services.AddRazorComponents()
    .AddInteractiveServerComponents();

var app = builder.Build();

// Configure the HTTP request pipeline.
if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler("/Error", createScopeForErrors: true);
    // The default HSTS value is 30 days. You may want to change this for production scenarios, see https://aka.ms/aspnetcore-hsts.
    app.UseHsts();
}
app.UseStatusCodePagesWithReExecute("/not-found");
app.UseHttpsRedirection();

app.UseAntiforgery();

app.MapStaticAssets();

// Reportes en PDF (RF017, RF018): el formulario de la página hace un GET normal a estas rutas,
// que piden el PDF a Java y lo devuelven como descarga (igual que el ejemplo del profesor).
app.MapGet("/reportes/tutor/descargar", DescargarReporteTutor);
app.MapGet("/reportes/sesiones/descargar", DescargarReporteSesiones);
app.MapGet("/reportes/reclamos/descargar", DescargarReporteReclamos);

app.MapRazorComponents<App>()
    .AddInteractiveServerRenderMode();

// Prueba temporal: bórrala cuando confirmes que funciona
/*app.MapGet("/probar-api", async (IHttpClientFactory fabrica) =>
{
    var http = fabrica.CreateClient("api");
    return Results.Content(await http.GetStringAsync("usuario"), "application/json");
});*/

app.Run();

static async Task<IResult> DescargarReporteTutor(int idTutor, DateOnly desde, DateOnly hasta,
    TutorPUCP.Servicios.SesionRestService cliente, HttpContext contexto, ILoggerFactory loggerFactory, CancellationToken ct)
{
    if (desde > hasta)
        return Results.BadRequest("La fecha «Desde» no puede ser posterior a «Hasta».");

    try
    {
        byte[] pdf = await cliente.DescargarReporteTutorAsync(idTutor, desde, hasta, ct);
        contexto.Response.Headers.CacheControl = "no-store";
        return Results.File(pdf, "application/pdf", "reporte-tutorias.pdf");
    }
    catch (Exception ex) when (!ct.IsCancellationRequested)
    {
        loggerFactory.CreateLogger("ReporteTutor").LogError(ex, "No se pudo obtener el PDF desde Java");
        return Results.Problem("No se pudo descargar el reporte. Revisa el servicio REST y vuelve a intentar.",
            statusCode: StatusCodes.Status502BadGateway);
    }
}

static async Task<IResult> DescargarReporteSesiones(DateOnly desde, DateOnly hasta,
    TutorPUCP.Servicios.SesionRestService cliente, HttpContext contexto, ILoggerFactory loggerFactory, CancellationToken ct)
{
    if (desde > hasta)
        return Results.BadRequest("La fecha «Desde» no puede ser posterior a «Hasta».");

    try
    {
        byte[] pdf = await cliente.DescargarReporteSesionesAsync(desde, hasta, ct);
        contexto.Response.Headers.CacheControl = "no-store";
        return Results.File(pdf, "application/pdf", "reporte-sesiones.pdf");
    }
    catch (Exception ex) when (!ct.IsCancellationRequested)
    {
        loggerFactory.CreateLogger("ReporteSesiones").LogError(ex, "No se pudo obtener el PDF desde Java");
        return Results.Problem("No se pudo descargar el reporte. Revisa el servicio REST y vuelve a intentar.",
            statusCode: StatusCodes.Status502BadGateway);
    }
}

static async Task<IResult> DescargarReporteReclamos(DateOnly desde, DateOnly hasta,
    TutorPUCP.Servicios.SesionRestService cliente, HttpContext contexto, ILoggerFactory loggerFactory, CancellationToken ct)
{
    if (desde > hasta)
        return Results.BadRequest("La fecha «Desde» no puede ser posterior a «Hasta».");

    try
    {
        byte[] pdf = await cliente.DescargarReporteReclamosAsync(desde, hasta, ct);
        contexto.Response.Headers.CacheControl = "no-store";
        return Results.File(pdf, "application/pdf", "reporte-reclamos.pdf");
    }
    catch (Exception ex) when (!ct.IsCancellationRequested)
    {
        loggerFactory.CreateLogger("ReporteReclamos").LogError(ex, "No se pudo obtener el PDF desde Java");
        return Results.Problem("No se pudo descargar el reporte. Revisa el servicio REST y vuelve a intentar.",
            statusCode: StatusCodes.Status502BadGateway);
    }
}
