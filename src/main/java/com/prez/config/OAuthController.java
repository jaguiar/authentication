package com.prez.config;

import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Controller
@SessionAttributes("authorizationRequest")
public class OAuthController {


    @GetMapping("/oauth/custom_confirm_access")
    public ModelAndView getAccessConfirmation(Map<String, Object> model, HttpServletRequest request) throws Exception {
        final String approvalContent = createTemplate(model, request);
        if (request.getAttribute("_csrf") != null) {
            model.put("_csrf", request.getAttribute("_csrf"));
        }
        View approvalView = new View() {
            @Override
            public String getContentType() {
                return "text/html";
            }

            @Override
            public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
                response.setContentType(getContentType());
                response.getWriter().append(approvalContent);
            }
        };
        return new ModelAndView(approvalView, model);
    }

    protected String createTemplate(Map<String, Object> model, HttpServletRequest request) {
        AuthorizationRequest authorizationRequest = (AuthorizationRequest) model.get("authorizationRequest");
        String clientId = authorizationRequest.getClientId();

        StringBuilder builder = new StringBuilder();
        builder.append("<html><head>" +
                "<meta charset=\"utf-8\">" +
                "<title>OAuth Approval</title>" +
                "<base href=\"/\">" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">" +
                "<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css\"/>" +
                "</head>");
        // body start
        builder.append("<body><div class=\"container-fluid mx-auto\">");

        builder.append("<div class=\"jumbotron text-center\" style=\"margin-bottom:0\"><h1><small><a href=\"/\">OAuth Approval</a></small></h1></div>");

        // first row
        builder.append("<div class=\"row py-4\"><div class=\"col\">Do you authorize \"").append(HtmlUtils.htmlEscape(clientId));
        builder.append("\" to access your protected resources?</div></div>");

        builder.append("<div class=\"form-group row mx-auto\">");
        // Confirmation form
        builder.append("<div class=\"col-xs-2\">");
        builder.append("<form id=\"confirmationForm\" name=\"confirmationForm\" action=\"");

        String requestPath = ServletUriComponentsBuilder.fromContextPath(request).build().getPath();
        if (requestPath == null) {
            requestPath = "";
        }

        builder.append(requestPath).append("/oauth/authorize\" method=\"post\">");
        builder.append("<input name=\"user_oauth_approval\" value=\"true\" type=\"hidden\"/>");

        String csrfTemplate = null;
        CsrfToken csrfToken = (CsrfToken) (model.containsKey("_csrf") ? model.get("_csrf") : request.getAttribute("_csrf"));
        if (csrfToken != null) {
            csrfTemplate = "<input type=\"hidden\" name=\"" + HtmlUtils.htmlEscape(csrfToken.getParameterName()) +
                    "\" value=\"" + HtmlUtils.htmlEscape(csrfToken.getToken()) + "\" />";
        }
        if (csrfTemplate != null) {
            builder.append(csrfTemplate);
        }

        String authorizeInputTemplate = "<button type=\"submit\" class=\"btn btn-primary\" name=\"authorize\">Authorize</button></form>";



        if (model.containsKey("scopes") || request.getAttribute("scopes") != null) {
            builder.append(createScopes(model, request));
            builder.append(authorizeInputTemplate);
            builder.append("</div>");
        } else {
            builder.append(authorizeInputTemplate);
            builder.append("</div>");
            // Denial form
            builder.append("<div class=\"col-xs-2 pl-2\">");
            builder.append("<form id=\"denialForm\" name=\"denialForm\" action=\"");
            builder.append(requestPath).append("/oauth/authorize\" method=\"post\">");
            builder.append("<input name=\"user_oauth_approval\" value=\"false\" type=\"hidden\"/>");
            if (csrfTemplate != null) {
                builder.append(csrfTemplate);
            }
            builder.append("<button type=\"submit\" class=\"btn border\" name=\"deny\">Deny</button></form>");
            builder.append("</div>");
        }
        builder.append("</div>"); // end form-group

        // body end
        builder.append("</div></body></html>");

        return builder.toString();
    }

    private CharSequence createScopes(Map<String, Object> model, HttpServletRequest request) {
        StringBuilder builder = new StringBuilder("<ul>");
        @SuppressWarnings("unchecked")
        Map<String, String> scopes = (Map<String, String>) (model.containsKey("scopes") ?
                model.get("scopes") : request.getAttribute("scopes"));
        for (String scope : scopes.keySet()) {
            String approved = "true".equals(scopes.get(scope)) ? " checked" : "";
            String denied = !"true".equals(scopes.get(scope)) ? " checked" : "";
            scope = HtmlUtils.htmlEscape(scope);

            builder.append("<li><div class=\"form-group\">");
            builder.append(scope).append(": <input type=\"radio\" name=\"");
            builder.append(scope).append("\" value=\"true\"").append(approved).append(">Approve</input> ");
            builder.append("<input type=\"radio\" name=\"").append(scope).append("\" value=\"false\"");
            builder.append(denied).append(">Deny</input></div></li>");
        }
        builder.append("</ul>");
        return builder.toString();
    }
}
