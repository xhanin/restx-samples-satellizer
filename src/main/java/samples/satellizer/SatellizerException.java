package samples.satellizer;

import restx.WebException;
import restx.http.HttpStatus;

/**
 * Date: 10/1/15
 * Time: 18:09
 */
public class SatellizerException extends WebException {
    public SatellizerException(HttpStatus status, String message) {
        super(status, message);
    }

    @Override
    public String getContentType() {
        return "application/json;UTF-8";
    }

    @Override
    public String getContent() {
        return "{ \"message\": \"" + super.getContent().replace("\"", "\\\"") + "\"}";
    }
}
