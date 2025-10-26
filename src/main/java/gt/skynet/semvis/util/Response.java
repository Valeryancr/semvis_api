package gt.skynet.semvis.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response {
    private Integer code;
    private String msg;
    private Object data;

    public static Response ok(Object data, String msg) {
        return new Response(200, msg, data);
    }

    public static Response error(String msg) {
        return new Response(400, msg, null);
    }

    public static Response notFound(String msg) {
        return new Response(404, msg, null);
    }
}
