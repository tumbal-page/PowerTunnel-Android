# Growtopia Redirect Plugin (skeleton)

Dua modul buat redirect client Growtopia (`growtopia1.com` / `growtopia2.com`) ke server pribadi (misalnya KingPS) lewat domain `lyu.my.id`, dengan target IP:port yang dinamis (playit.gg tunnel, dsb).

## Kenapa dua modul

- `gt-redirect-http/` — dipakai kalau request `server_data.php` dari client masih **plain HTTP** (bukan HTTPS). Ini modul yang paling ringan: cukup intercept di level HTTP proxy, tanpa cert/MITM sama sekali.
- `gt-redirect-https/` — dipakai kalau ternyata versi client Growtopia yang kamu target udah pindah ke **HTTPS** buat `server_data.php`. Modul ini extend modul HTTP tapi nambah layer MITM (LittleProxy-MITM) buat decrypt/rewrite/re-encrypt.

Cek dulu pakai traffic capture (mitmproxy / Charles / tcpdump) ke APK Growtopia target sebelum milih modul mana yang dipakai. Traffic ENet (UDP, gameplay) SELALU di-handle sama, gak butuh cert di kedua modul — itu murni NAT rewrite di level VpnService/tun interface, bukan tanggung jawab modul ini.

## Format config (custom, bukan /etc/hosts asli)

File hosts OS cuma support `IP domain`, jadi kita gak bisa nulis `lyu.my.id growtopia1.com` secara literal. Sebagai gantinya, modul ini baca config custom (JSON) yang bisa di-fetch dari URL remote atau file lokal:

```json
{
  "target_domain": "lyu.my.id",
  "override_domains": ["growtopia1.com", "growtopia2.com"],
  "config_endpoint": "https://lyu.my.id/gt-config.json",
  "fallback_port": 17091
}
```

Alur baca config:
1. App resolve `target_domain` (`lyu.my.id`) ke IP terkini via DNS lookup biasa.
2. App fetch `config_endpoint` (HTTP/HTTPS biasa, boleh lewat Cloudflare proxy karena ini traffic HTTP normal) buat dapetin port publik terkini dari playit.gg tunnel (karena port ini random tiap restart tunnel).
3. Hasil IP + port itu yang disuntikkan ke body response `server_data.php` yang di-serve balik ke client Growtopia.

Contoh isi `config.example.json` ada di masing-masing folder modul.

## Status

Ini skeleton awal (stub), belum wired ke PowerTunnel plugin SDK (`io.github.krlvm.powertunnel.sdk`) yang biasanya jadi dependency terpisah buat plugin PowerTunnel (lihat proyek `LibertyTunnel` / `PowerTunnel-DNS` / `PowerTunnel-AdBlock` sebagai referensi struktur plugin resmi). Perlu ditambahin:
- `build.gradle` plugin module + dependency ke PowerTunnel SDK
- Implementasi `HttpFiltersSourceAdapter` (atau adapter yang setara) buat intercept request/response ke `growtopia1.com`/`growtopia2.com`
- Wiring NAT/port-rewrite buat traffic ENet di `TunnelingVpnService`
