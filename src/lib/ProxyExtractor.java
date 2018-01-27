package lib;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Texhnolyze
*/
public class ProxyExtractor {
    
//  Available protocols are HTTP, HTTPS, SOCKS4, SOCKS5
    
    private final Set<ExtractedProxy> proxies = new HashSet<>();
    
//  https query parameters. you can find them on http://proxydb.net/
    private final Map<String, String> params;
    
    private int offset;
    
    public ProxyExtractor() {
        this(Collections.EMPTY_MAP);
    }
    
    public ProxyExtractor(Map<String, String> params) {
        this.params = params;
    }
    
    public int getOffset() {
        return offset;
    }
    
    public void setOffset(int offset) {
        this.offset = offset;
    }
    
    public Map<String, String> params() {
        return params;
    }
    
    public void reset() {
        offset = 0;
        proxies.clear();
    }
    
    public Set<ExtractedProxy> getExtractedProxies() {
        return proxies;
    }
    
//  Tries to extract proxies and returns num of proxies extracted.
//  If no proxies extracted returns 0.
    public int extract() throws IOException {
        
        String query = "http://proxydb.net/?offset=" + offset + "&";
        for (Map.Entry<String, String> param : params.entrySet()) 
            query += param.getKey() + "=" + param.getValue() + "&";
        
        Document doc = Jsoup.connect(query).get();
        Elements table = doc.getElementsByClass("table-responsive");
        
        if (table.isEmpty()) return 0;
        
        table = table.first().children().last().children().last().children();
        int count = table.size();
        int n = 0;        
        
        for (int i = 0; i < count; i++) {
            
            Element row = table.get(i);
            
            String host = row.children().get(1).html();
            String country = row.children().get(2).children().last().html();
            String isp = row.children().get(3).children().first().html();
            String protocol = row.children().get(4).html();
            String anonymity = row.children().get(5).children().first().html();
            String uptime = row.children().get(6).children().first().html();
            String responseTime = row.children().get(7).children().first().html();
            String checked = row.children().get(10).children().first().html();

            ExtractedProxy ep = new ExtractedProxy();
            
            Map<String, String> m = ep.props;
            m.put("host", host);
            m.put("country", country);
            m.put("isp", isp);
            m.put("protocol", protocol);
            m.put("anonymity", anonymity);
            m.put("uptime", uptime);
            m.put("responseTime", responseTime);
            m.put("checked", checked);
            
            String script = row.children().first().children().first().html();
            String[] proxyString = decipher(script);

            Type type = host.equals("HTTP") || host.equals("HTTPS") ? Type.HTTP : Type.SOCKS;
            
            ep.proxy = new Proxy(
                    type, 
                    InetSocketAddress.createUnresolved(
                            proxyString[0],
                            Integer.parseInt(
                                    proxyString[1]
                            )
                    )
            );
            
            if (proxies.add(ep)) n++;
            
        }

        offset += count;
        
        return n;
        
    }
    
    private static final Pattern PART1 = Pattern.compile("([0-9]+)?\\.?([0-9]+\\.)+[0-9]+");
    private static final Pattern PART2 = Pattern.compile("(\\\\x[0-9][0-9a-f])+");
    private static final Pattern PART3 = Pattern.compile("-?[0-9]+\\s(\\+|-)\\s[0-9]+");
    
    private static String[] decipher(String script) {
        
        StringBuilder sb = new StringBuilder();
        
        Matcher p1 = PART1.matcher(script), 
                p2 = PART2.matcher(script), 
                p3 = PART3.matcher(script);
        
        p1.find();
        sb.append(script.substring(p1.start(), p1.end())).reverse();

        p2.find(p1.end());
        String temp = script.substring(p2.start(), p2.end());
        String[] split = temp.split("\\\\x");
        String base64 = "";
        
        for (int i = 1; i < split.length; i++) {
            int n = Integer.parseInt(split[i], 16);
            base64 += fromCharCode(n);
        }
        
        sb.append(decode(base64));
        
        p3.find(p2.end());
        temp = script.substring(p3.start(), p3.end());
        split = temp.split("\\s");
        
        int l = Integer.parseInt(split[0]);
        int r = Integer.parseInt(split[2]);
        
        int port = -1;
        
        if (split[1].equals("+")) port = l + r;
        else if (split[1].equals("-")) port = l - r;
        
        sb.append(":").append(port);
        
        return sb.toString().split(":");
        
    }
    
    private static String fromCharCode(int... codePoints) {
        StringBuilder sb = new StringBuilder(codePoints.length);
        for (int codePoint : codePoints) sb.append(Character.toChars(codePoint));
        return sb.toString();
    }
    
    private static String decode(String s) {
        return new String(Base64.getDecoder().decode(s));
    }
    
    public static class ExtractedProxy {
        
        private Proxy proxy;
        private Map<String, String> props = new HashMap<>();

        public Proxy getProxy() {
            return proxy;
        }
        
        public Map<String, String> getProps() {
            return Collections.unmodifiableMap(props);
        }
        
        @Override
        public int hashCode() {
            int hash = 7;
            hash = 53 * hash + Objects.hashCode(this.proxy);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            final ExtractedProxy other = (ExtractedProxy) obj;
            return Objects.equals(this.proxy, other.proxy);
        }
        
    }
    
}
