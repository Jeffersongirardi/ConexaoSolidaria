import sharp from "sharp";
import { mkdirSync } from "node:fs";

mkdirSync("public/icons", { recursive: true });

const svg = (size) => `<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" width="${size}" height="${size}" viewBox="0 0 ${size} ${size}">
  <rect width="${size}" height="${size}" rx="${size * 0.2}" fill="#0d6efd"/>
  <path d="M ${size * 0.5} ${size * 0.78} C ${size * 0.2} ${size * 0.55} ${size * 0.22} ${size * 0.3} ${size * 0.38} ${size * 0.3} C ${size * 0.46} ${size * 0.3} ${size * 0.5} ${size * 0.36} ${size * 0.5} ${size * 0.36} C ${size * 0.5} ${size * 0.36} ${size * 0.54} ${size * 0.3} ${size * 0.62} ${size * 0.3} C ${size * 0.78} ${size * 0.3} ${size * 0.8} ${size * 0.55} ${size * 0.5} ${size * 0.78} Z" fill="#ffffff"/>
</svg>`;

for (const [name, size, pad] of [["icon-192.png", 192, 0], ["icon-512.png", 512, 0], ["maskable-512.png", 512, 64]]) {
  const inner = size - pad * 2;
  const bg = `<svg xmlns="http://www.w3.org/2000/svg" width="${size}" height="${size}"><rect width="${size}" height="${size}" fill="#0d6efd"/></svg>`;
  const fg = svg(inner);
  await sharp(Buffer.from(bg))
    .composite([{ input: Buffer.from(fg), left: pad, top: pad }])
    .png()
    .toFile(`public/icons/${name}`);
  console.log("gerado", name);
}
