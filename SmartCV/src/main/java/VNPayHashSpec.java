import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class VNPayHashSpec {
    public static void main(String[] args) throws Exception {
        String raw = "Thanh toan goi PRO";
        String utf8 = URLEncoder.encode(raw, StandardCharsets.UTF_8.toString());
        String ascii = URLEncoder.encode(raw, StandardCharsets.US_ASCII.toString());
        System.out.println("Raw: " + raw);
        System.out.println("UTF-8: " + utf8);
        System.out.println("US-ASCII: " + ascii);
    }
}
