package id.my.lyu.gtredirect.http;

/**
 * Skeleton plugin: redirect growtopia1.com / growtopia2.com server_data.php
 * response (plain HTTP, no TLS) ke server target (KingPS via lyu.my.id).
 *
 * Model jaringan: A record biasa (lyu.my.id -> IP playit terkini, DNS-only,
 * bukan proxied Cloudflare) + port STATIS (dari tunnel game playit.gg).
 * Gak ada fetch config HTTP terpisah, gak ada SRV record -- cuma DNS resolve
 * biasa + port konstanta.
 *
 * TODO:
 * - Tambah dependency PowerTunnel SDK di build.gradle module ini
 * - Implement request/response filter yang cuma aktif untuk host di
 *   override_domains (biarkan traffic lain passthrough tanpa disentuh)
 * - Panggil resolveTargetAndBuildResponse() di response filter, replace body
 *   asli dengan hasilnya sebelum diteruskan ke client
 */
public class GrowtopiaRedirectHttpPlugin {

    private final String targetDomain;
    private final int staticPort;

    public GrowtopiaRedirectHttpPlugin(String targetDomain, int staticPort) {
        this.targetDomain = targetDomain;
        this.staticPort = staticPort;
    }

    /**
     * Resolve target_domain (lyu.my.id) ke IP terkini via DNS biasa, susun
     * body server_data.php format Growtopia:
     *
     *   server|<ip>
     *   port|<static_port>
     *   type|1
     *
     * Port TIDAK pernah di-fetch dinamis -- ini konstanta karena tunnel game
     * playit.gg yang dipakai punya port publik statis. Yang berubah cuma IP.
     *
     * @return teks body pengganti untuk dikirim balik ke client Growtopia
     */
    public String resolveTargetAndBuildResponse() {
        String ip = resolveIp(targetDomain);

        StringBuilder sb = new StringBuilder();
        sb.append("server|").append(ip).append("\n");
        sb.append("port|").append(staticPort).append("\n");
        sb.append("type|1\n");
        return sb.toString();
    }

    private String resolveIp(String domain) {
        // TODO: java.net.InetAddress.getByName(domain).getHostAddress()
        // WAJIB di background thread (bukan main thread Android).
        // Jangan cache terlalu lama -- resolve ulang tiap ada request masuk,
        // karena IP inilah yang berubah-ubah (bukan port).
        throw new UnsupportedOperationException("TODO: implement DNS resolve");
    }

    /**
     * Cek apakah host request ini termasuk yang harus di-redirect.
     * Traffic host lain harus passthrough tanpa modifikasi.
     */
    public static boolean shouldIntercept(String host, java.util.List<String> overrideDomains) {
        return overrideDomains.contains(host);
    }
}
