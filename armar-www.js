// Arma www/index.html a partir de app.html (la misma app que la página de Claude),
// agregándole la cabecera de documento que necesita la app de Android.
const fs = require('fs');
const cuerpo = fs.readFileSync('app.html', 'utf8');
const html = `<!doctype html><html lang="es"><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1,viewport-fit=cover">
<style>:root{padding-top:env(safe-area-inset-top,0px);padding-bottom:env(safe-area-inset-bottom,0px)}body{margin:0;font:14px system-ui,sans-serif}img{max-width:100%}[hidden]{display:none!important}</style>
</head><body>
${cuerpo}
</body></html>`;
fs.mkdirSync('www', { recursive: true });
fs.writeFileSync('www/index.html', html);
console.log('www/index.html listo');
