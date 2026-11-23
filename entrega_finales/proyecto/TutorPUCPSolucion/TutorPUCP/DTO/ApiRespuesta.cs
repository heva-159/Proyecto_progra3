namespace TutorPUCP.DTO
{
    public static class ApiRespuesta
    {
        public static async Task<string?> LeerErrorAsync(this HttpResponseMessage respuesta)
        {
            try
            {
                var cuerpo = await respuesta.Content.ReadFromJsonAsync<Dictionary<string, string>>();
                return cuerpo is not null && cuerpo.TryGetValue("error", out var mensaje) ? mensaje : null;
            }
            catch (Exception ex) when (ex is System.Text.Json.JsonException or NotSupportedException)
            {
                return null; // la respuesta no era JSON
            }
        }
    }
}
