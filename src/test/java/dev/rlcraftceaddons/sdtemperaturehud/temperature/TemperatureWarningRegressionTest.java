package dev.rlcraftceaddons.sdtemperaturehud.temperature;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class TemperatureWarningRegressionTest {
    @Test
    public void preservesDangerWarningAndSafeBoundaries() {
        assertSame(TemperatureWarningLevel.CRITICAL_COLD, classify(Integer.MIN_VALUE));
        assertSame(TemperatureWarningLevel.CRITICAL_COLD, classify(2));
        assertSame(TemperatureWarningLevel.CAUTION_COLD, classify(3));
        assertSame(TemperatureWarningLevel.CAUTION_COLD, classify(5));
        assertSame(TemperatureWarningLevel.NORMAL, classify(6));
        assertSame(TemperatureWarningLevel.NORMAL, classify(19));
        assertSame(TemperatureWarningLevel.CAUTION_HOT, classify(20));
        assertSame(TemperatureWarningLevel.CAUTION_HOT, classify(22));
        assertSame(TemperatureWarningLevel.CRITICAL_HOT, classify(23));
        assertSame(TemperatureWarningLevel.CRITICAL_HOT, classify(Integer.MAX_VALUE));
    }

    @Test
    public void exposesOnePublicWarningClassifier() {
        int classifiers = 0;
        for (Method method : TemperatureWarning.class.getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers())
                && Modifier.isStatic(method.getModifiers())
                && method.getReturnType() == TemperatureWarningLevel.class) {
                ++classifiers;
            }
        }

        assertEquals(1, classifiers);
    }

    private static TemperatureWarningLevel classify(int bodyTemperature) {
        return TemperatureWarning.classifyBodyTemperature(bodyTemperature);
    }
}
