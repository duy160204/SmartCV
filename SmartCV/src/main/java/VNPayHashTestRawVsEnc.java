import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class VNPayHashTestRawVsEnc {
    public static void main(String[] args) throws Exception {
        String dataUrlEncode = "vnp_OrderInfo=" + URLEncoder.encode("Thanh toan goi PRO", "US-ASCII");
        String dataRaw = "vnp_OrderInfo=Thanh toan goi PRO";
        System.out.println("Encoded: " + dataUrlEncode);
        System.out.println("Raw    : " + dataRaw);
    }
}
