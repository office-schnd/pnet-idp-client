package at.porscheinformatik.idp.saml2.response;

import static at.porscheinformatik.idp.saml2.AuthnContextClass.*;
import static at.porscheinformatik.idp.saml2.PartnerNetSaml2AuthenticationRequestUtils.*;
import static org.springframework.util.CollectionUtils.*;

import at.porscheinformatik.idp.saml2.AuthnContextClass;
import at.porscheinformatik.idp.saml2.HttpRequestContextAwareSaml2AuthenticationDetailsSource.HttpRequestContext;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.handler.MessageHandlerException;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Response;

public class VerifyAuthenticationStrenghMessageHandler extends AbstractSimpleMessageHandler {

    @Override
    public void invoke(MessageContext messageContext) throws MessageHandlerException {
        HttpRequestContext httpRequestContext = getHttpRequestContext(messageContext);

        Integer requestedNistLevel = getRequestedNistLevel(httpRequestContext.getRequest());

        // Nothing special requested, so everything is fine
        if (requestedNistLevel == null) {
            return;
        }

        Response response = getResponse(messageContext);
        Assertion assertion = firstElement(response.getAssertions());
        AuthnStatement authnStatement = firstElement(assertion.getAuthnStatements());
        AuthnContextClassRef authnContextClassRef = authnStatement.getAuthnContext().getAuthnContextClassRef();

        AuthnContextClass authnContextClass = fromReference(authnContextClassRef.getURI()).orElseThrow(() ->
            new MessageHandlerException(
                String.format(
                    "Could not validate authentiation strenght as %s is unkown",
                    authnContextClassRef.getURI()
                )
            )
        );

        if (authnContextClass.getNistLevel() < requestedNistLevel) {
            throw new MessageHandlerException(
                String.format(
                    "Authentication strength %s is weaker than requested strength %s",
                    authnContextClass,
                    requestedNistLevel
                )
            );
        }
    }
}
