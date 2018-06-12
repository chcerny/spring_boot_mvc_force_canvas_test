package com.cerny.test.demo;

import canvas.SignedRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.beans.factory.annotation.Value;

@Controller
public class CanvasController {
    @Value("${canvas.client.secret}")
    private String canvasClientSecret;

    // curl http://localhost:8080/canvas?signed_request=123
    // curl -k https://localhost:8080/canvas?signed_request=123
    // curl -H "Authorization: Basic 456" -X GET http://localhost:8080/canvas?signed_request=123
    // curl -H "Authorization: Basic 456" -X GET -k https://localhost:8080/canvas?signed_request=123
    // curl -d "signed_request=123" -H "Authorization: Basic 456" -X POST http://localhost:8080/canvas
    // curl -d "signed_request=123" -H "Authorization: Basic 456" -X POST -k https://localhost:8080/canvas
    @RequestMapping(value = "/canvas", method = { RequestMethod.GET, RequestMethod.POST })
    public String canvas(@RequestParam(name="signed_request", required=false, defaultValue="signed_request") String signedRequest,
                         @RequestHeader(name="Authorization", required=false) String authorizationHeader,
                         Model model) {
        String[] authorizationHeaderParts;
        String accessToken = null;
        String jsonRequestToken = null;
        String jsonRequestTokenPretty = null;

        if (authorizationHeader != null) {
            authorizationHeaderParts = authorizationHeader.split(" ");
            if (authorizationHeaderParts.length > 1)
                accessToken = authorizationHeaderParts[1];
        }

        try {
            jsonRequestToken = SignedRequest.verifyAndDecodeAsJson(signedRequest, canvasClientSecret);

            // "pretty" formatting of the json string
            ObjectMapper mapper = new ObjectMapper();
            Object json = mapper.readValue(jsonRequestToken, Object.class);
            jsonRequestTokenPretty = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch(Exception exp) {
            // TODO: quick hack must be implemented properly
            jsonRequestToken = new String("<NO VALID SIGNED REQUEST!>");
            jsonRequestTokenPretty = jsonRequestToken;
        }

        model.addAttribute("signed_request", signedRequest);
        model.addAttribute("canvas_client_secret", canvasClientSecret);
        model.addAttribute("authorization", authorizationHeader);
        model.addAttribute("access_token", accessToken);
        model.addAttribute("json_request_token", jsonRequestTokenPretty);

        return "canvas"; // view name which is rendered by thymeleaf (canvas.html)
    }

    // Does not work, when a GET request is sent no request parameters in the header are provided
    // curl -d "signed_request=123" -H "Authorization: Basic 456" -X GET http://localhost:8080/canvas

}