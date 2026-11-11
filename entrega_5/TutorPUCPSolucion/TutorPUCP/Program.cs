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
app.MapRazorComponents<App>()
    .AddInteractiveServerRenderMode();

// Prueba temporal: bórrala cuando confirmes que funciona
/*app.MapGet("/probar-api", async (IHttpClientFactory fabrica) =>
{
    var http = fabrica.CreateClient("api");
    return Results.Content(await http.GetStringAsync("usuario"), "application/json");
});*/

app.Run();
