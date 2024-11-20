package at.porscheinformatik.idp.saml2;

import static at.porscheinformatik.idp.saml2.PartnerNetSaml2AuthenticationRequestUtils.*;
import static at.porscheinformatik.idp.saml2.Saml2Utils.*;

import at.porscheinformatik.idp.saml2.HttpRequestContextAwareSaml2AuthenticationDetailsSource.HttpRequestContext;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.Optional;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticationToken;

public class HttpRequestContextAwareSaml2AuthenticationDetailsSource
    implements AuthenticationDetailsSource<HttpServletRequest, HttpRequestContext> {

    @Override
    public HttpRequestContext buildDetails(HttpServletRequest request) {
        return new HttpRequestContext(request);
    }

    public static class HttpRequestContext {

        public static HttpRequestContext fromToken(Saml2AuthenticationToken token) {
            Object details = Objects.requireNonNull(
                token.getDetails(),
                "No authentication details found. Ensure to add a HttpRequestContext to the authentication details."
            );

            if (!HttpRequestContext.class.isAssignableFrom(details.getClass())) {
                throw new IllegalArgumentException(
                    String.format("AuthenticationDetails %s are not of type HttpRequestContext", details)
                );
            }

            return (HttpRequestContext) details;
        }

        private final HttpServletRequest request;

        public HttpRequestContext(HttpServletRequest request) {
            super();
            this.request = request;
        }

        public HttpServletRequest getRequest() {
            return request;
        }

        public Optional<String> getAuthnRequestId() {
            return retrieveAuthnRequestId(getRequest());
        }

        public String getClientAddress() {
            return request.getRemoteAddr();
        }

        public boolean isForceAuthentication() {
            return forceAuthenticationRequested(request);
        }

        public Integer getSessionAge() {
            return sessionAgeRequested(request);
        }
    }
}
