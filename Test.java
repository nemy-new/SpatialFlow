import java.net.URI;
public class Test {
    public static void main(String[] args) throws Exception {
        URI uri = new URI("innertube://abc_def");
        System.out.println("host: " + uri.getHost());
        System.out.println("authority: " + uri.getAuthority());
        System.out.println("toString: " + uri.toString());
    }
}
