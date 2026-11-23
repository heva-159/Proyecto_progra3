// Modo oscuro y descargas de TutorPUCP.
window.tp = {
    leerTema: function () {
        try { return localStorage.getItem("tp-tema") || "light"; } catch (e) { return "light"; }
    },
    ponerTema: function (tema) {
        document.documentElement.setAttribute("data-bs-theme", tema);
        try { localStorage.setItem("tp-tema", tema); } catch (e) { }
    },
    descargar: function (nombre, tipo, base64) {
        const bytes = Uint8Array.from(atob(base64), c => c.charCodeAt(0));
        const url = URL.createObjectURL(new Blob([bytes], { type: tipo }));
        const a = document.createElement("a");
        a.href = url;
        a.download = nombre;
        document.body.appendChild(a);
        a.click();
        a.remove();
        setTimeout(() => URL.revokeObjectURL(url), 1000);
    },
    // Selector de emojis (librería emoji-picker-element, en español). Avisa a Blazor al elegir uno.
    emojis: async function (contenedor, dotnet) {
        const base = "https://cdn.jsdelivr.net/npm/emoji-picker-element@1.26.3/";
        if (!customElements.get("emoji-picker")) await import(base + "index.js");
        const es = (await import(base + "i18n/es.js")).default;
        const p = document.createElement("emoji-picker");
        p.locale = "es";
        p.i18n = es;
        p.dataSource = "https://cdn.jsdelivr.net/npm/emoji-picker-element-data@1.7.1/es/cldr/data.json";
        p.classList.add(document.documentElement.getAttribute("data-bs-theme") === "dark" ? "dark" : "light");
        p.addEventListener("emoji-click", e => dotnet.invokeMethodAsync("Elegir", e.detail.unicode));
        contenedor.replaceChildren(p);
    }
};
