# Growtopia Redirect Plugin (skeleton) — versi A-record + port statis

Dua modul buat redirect client Growtopia (`growtopia1.com` / `growtopia2.com`) ke server pribadi (KingPS) lewat domain `lyu.my.id`.

## Model jaringan (final, disederhanakan)

Karena tunnel playit.gg yang dipakai adalah tunnel khusus game (mis. slot Minecraft/Terraria yang di-repurpose), **port publiknya statis** — cuma **IP relay yang berubah-ubah** (tergantung restart agent / reassignment relay). Jadi:

```
Client Growtopia (APK)
        ↓ ENet (UDP) ke [IP]:[PORT TETAP]
       IP    -> berubah-ubah, di-resolve dari lyu.my.id (A record)
       PORT  -> konstanta, ditulis di config, TIDAK pernah difetch dinamis
```

Gak butuh SRV record atau fetch config HTTP terpisah. Cukup:

1. **A record biasa di Cloudflare**: `lyu.my.id -> IP playit terkini`, proxy status **DNS only (grey-cloud)** — BUKAN proxied (orange-cloud), karena orange-cloud cuma proxy HTTP/HTTPS, bukan raw ENet/UDP.
2. Tiap kali IP playit berubah (mis. abis restart agent), tinggal **update A record itu manual/API** di Cloudflare. Gak perlu update APK, config lain, atau republish apapun — App selalu resolve `lyu.my.id` on-the-fly tiap ada request masuk.
3. Port disimpen sebagai **konstanta di config**, karena statis dari tunnel game playit.

## Kenapa masih dua modul (HTTP vs HTTPS)

- `gt-redirect-http/` — dipakai kalau request `server_data.php` client masih **plain HTTP**. Paling ringan, tanpa cert/MITM.
- `gt-redirect-https/` — dipakai kalau ternyata client target udah pindah ke **HTTPS**. Modul ini extend modul HTTP + tambah layer MITM (LittleProxy-MITM), MITM cuma aktif untuk host di `override_domains` (cek SNI dulu sebelum decide decrypt).

Cek dulu pakai traffic capture (mitmproxy/Charles/tcpdump) ke APK Growtopia target buat mastiin plain HTTP atau HTTPS sebelum milih modul mana yang dipakai.

Traffic ENet (UDP, gameplay) SELALU passthrough/NAT-rewrite biasa di kedua modul — gak ada cert/TLS di situ sama sekali.

## Format config

```json
{
  "target_domain": "lyu.my.id",
  "override_domains": ["growtopia1.com", "growtopia2.com"],
  "static_port": 43210
}
```

`static_port` diisi port publik yang di-assign playit buat tunnel game kamu (cek dashboard playit, bukan port lokal 17091 — 17091 itu urusan internal server, client publik gak perlu tau).

## Status

Skeleton (stub), belum wired ke PowerTunnel plugin SDK (`io.github.krlvm.powertunnel.sdk`). Perlu ditambahin:
- `build.gradle` plugin module + dependency ke PowerTunnel SDK
- Implementasi `HttpFiltersSourceAdapter` (atau adapter setara) buat intercept request/response ke `growtopia1.com`/`growtopia2.com`
- Wiring NAT/port-rewrite buat traffic ENet di `TunnelingVpnService`
