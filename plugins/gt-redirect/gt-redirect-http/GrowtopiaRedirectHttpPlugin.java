package id.my.lyu.gtredirect.http;

/**
 * Skeleton plugin: redirect growtopia1.com / growtopia2.com server_data.php
 * response (plain HTTP, no TLS) ke server target (misalnya KingPS via lyu.my.id).
 *
 * Belum di-wire ke PowerTunnel plugin SDK asli -- ini kerangka logika inti
 * (config loading + response rewriting) yang nanti tinggal disambungkan ke
 * HttpFiltersSourceAdapter / equivalent dari io.github.krlvm.powertunnel.sdk.
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
    private final String configEndpoint;
    private final int fallbackPort;

    public GrowtopiaRedirectHttpPlugin(String targetDomain, String configEndpoint, int fallbackPort) {
        this.targetDomain = targetDomain;
        this.configEndpoint = configEndpoint;
        this.fallbackPort = fallbackPort;
    }

    /**
     * Resolve target_domain ke IP terkini, fetch port terbaru dari
     * config_endpoint (mis. port playit.gg tunnel yang random tiap restart),
     * lalu susun body server_data.php format Growtopia:
     *
     *   server|<ip>
     *   port|<port>
     *   type|1
     *
     * @return teks body pengganti untuk dikirim balik ke client Growtopia
     */
    public String resolveTargetAndBuildResponse() {
        String ip = resolveIp(targetDomain);
        int port = fetchCurrentPort(configEndpoint, fallbackPort);

        StringBuilder sb = new StringBuilder();
        sb.append("server|").append(ip).append("\n");
        sb.append("port|").append(port).append("\n");
        sb.append("type|1\n");
        return sb.toString();
    }

    private String resolveIp(String domain) {
        // TODO: java.net.InetAddress.getByName(domain).getHostAddress()
        // Lakukan di background thread (bukan main thread Android).
        throw new UnsupportedOperationException("TODO: implement DNS resolve");
    }

    private int fetchCurrentPort(String endpoint, int fallback) {
        // TODO: HTTP GET ke endpoint, parse JSON { "port": 43210 }
        // Kalau fetch gagal / timeout, pakai fallback (fallbackPort).
        throw new UnsupportedOperationException("TODO: implement config fetch");
    }

    /**
     * Cek apakah host request ini termasuk yang harus di-redirect.
     * Traffic host lain harus passthrough tanpa modifikasi.
     */
    public static boolean shouldIntercept(String host, java.util.List<String> overrideDomains) {
        return overrideDomains.contains(host);
    }
}
