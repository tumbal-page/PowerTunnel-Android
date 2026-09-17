package id.my.lyu.gtredirect.https;

import id.my.lyu.gtredirect.http.GrowtopiaRedirectHttpPlugin;

/**
 * Varian HTTPS dari GrowtopiaRedirectHttpPlugin -- dipakai HANYA kalau
 * traffic growtopia1.com / growtopia2.com ternyata sudah TLS (bukan plain
 * HTTP lagi). Reuse logic resolve/build-response dari modul HTTP (A record +
 * port statis), tambah layer MITM decrypt/re-encrypt.
 *
 * TODO:
 * - Wire ke LittleProxy-MITM (dependency PowerTunnel core untuk fitur HTTPS
 *   filtering)
 * - Pastikan CertificateManager (io.github.krlvm.powertunnel.android.managers.
 *   CertificateManager di app module) sudah generate & install CA lokal
 *   sebelum modul ini aktif -- user harus approve instalasi cert sekali di
 *   awal
 * - MITM HANYA aktif untuk host di override_domains (cek SNI di TLS
 *   ClientHello dulu sebelum decide decrypt atau passthrough) -- traffic
 *   HTTPS lain jangan di-MITM, biar ringan & gak melanggar privasi user
 * - Traffic ENet (UDP) tetap TIDAK butuh modul ini -- itu murni NAT rewrite
 *   terpisah, gak ada TLS di situ
 */
public class GrowtopiaRedirectHttpsPlugin {

    private final GrowtopiaRedirectHttpPlugin delegate;
    private final String mitmCaAlias;

    public GrowtopiaRedirectHttpsPlugin(String targetDomain, int staticPort, String mitmCaAlias) {
        this.delegate = new GrowtopiaRedirectHttpPlugin(targetDomain, staticPort);
        this.mitmCaAlias = mitmCaAlias;
    }

    /**
     * Sama seperti versi HTTP, cuma dipanggil dari dalam TLS filter setelah
     * traffic sudah didecrypt oleh MITM layer.
     */
    public String resolveTargetAndBuildResponse() {
        return delegate.resolveTargetAndBuildResponse();
    }

    /**
     * Dipanggil sebelum decrypt: cek SNI dari ClientHello, cuma decrypt kalau
     * host-nya emang salah satu override_domains. Host lain -> passthrough
     * raw TLS tanpa MITM sama sekali (hemat CPU + gak melanggar privasi).
     */
    public boolean shouldDecrypt(String sniHost, java.util.List<String> overrideDomains) {
        return overrideDomains.contains(sniHost);
    }
}
