package dev.rlcraftceaddons.sdtemperaturehud.integration.xaero;

import org.junit.Test;
import static org.junit.Assert.*;

public class XaeroOptionalLoadingTest {
    @Test public void nativeInfoVersionChecksDoNotResumeAreaSampling() {
        assertEquals("ready",XaeroIntegration.status(true,"26.5.1","1.7.3"));
        assertEquals("ready",XaeroIntegration.status(true,"26.6.0","1.7.3"));
        assertEquals("missing",XaeroIntegration.status(true,null,"1.7.3"));
        assertEquals("unsupported",XaeroIntegration.status(true,"27.0.0","1.7.3"));
        assertEquals("unsupported",XaeroIntegration.status(true,"26.6.0",null));
        assertFalse(XaeroIntegration.areaSamplingEnabled());
    }
    @Test public void acceptsOnlyTheInspectedVersions() {
        assertTrue(XaeroIntegration.supported(true,"26.5.1"));
        assertTrue(XaeroIntegration.supported(true,"26.6.0"));
        assertTrue(XaeroIntegration.supported(false,"1.46.0"));
        assertTrue(XaeroIntegration.supported(false,"1.47.0"));
        assertFalse(XaeroIntegration.supported(true,null));
        assertFalse(XaeroIntegration.supported(true,"27.0.0"));
        assertFalse(XaeroIntegration.supported(false,"26.6.0"));
    }
    @Test public void rotatingProjectionKeepsLabelsUprightAndUsesWorldOffsets() {
        double[] north=MapProjection.minimap(10,0,0,1,2);
        assertEquals(0,north[0],.0001);assertEquals(20,north[1],.0001);
        double[] rotated=MapProjection.minimap(10,0,1,0,2);
        assertEquals(20,rotated[0],.0001);assertEquals(0,rotated[1],.0001);
        double[] world=MapProjection.world(-20,30,2);
        assertEquals(-40,world[0],0);assertEquals(60,world[1],0);
    }
    @Test public void registersOncePerHandlerAndFailureIsLocal() {
        RegistrationGate mini=new RegistrationGate(),world=new RegistrationGate();Object handler=new Object();
        assertFalse(mini.needsRegistration(null));assertTrue(mini.needsRegistration(handler));
        mini.attached(handler);assertFalse(mini.needsRegistration(handler));
        assertTrue(mini.needsRegistration(new Object()));mini.fail();
        assertFalse(mini.needsRegistration(new Object()));assertTrue(world.needsRegistration(handler));
    }
}
