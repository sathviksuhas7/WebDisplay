
package com.epdcl.webdisplay.service.security;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentSecurityServiceTest {

    @Test
    void shouldRejectEicarWhenClamAvDetectsIt() throws Exception {

        DocumentSecurityService service =
                new DocumentSecurityService(
                        true, "127.0.0.1", 3310, 10000);

        Method scanMethod =
                DocumentSecurityService.class.getDeclaredMethod(
                        "scanWithClamAv", byte[].class);

        scanMethod.setAccessible(true);

        byte[] eicar = (
                "X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*"
        ).getBytes(StandardCharsets.US_ASCII);

        InvocationTargetException exception = assertThrows(
                InvocationTargetException.class,
                () -> scanMethod.invoke(service, (Object) eicar)
        );

        assertTrue(
                exception.getCause() instanceof IllegalArgumentException
        );

        assertTrue(
                exception.getCause().getMessage().contains("malware")
        );
    }
}
