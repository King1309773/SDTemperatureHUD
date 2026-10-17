package dev.rlcraftceaddons.sdtemperaturehud.client;

import java.io.IOException;
import java.lang.reflect.Method;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TemperatureAnalysisKeyTest {
    @Test
    public void holdMirrorsTheCurrentKeyState() {
        TemperatureAnalysisKey state = new TemperatureAnalysisKey();

        assertFalse(state.isVisible(AnalysisDisplayMode.HOLD, false));
        assertTrue(state.isVisible(AnalysisDisplayMode.HOLD, true));
        assertFalse(state.isVisible(AnalysisDisplayMode.HOLD, false));
    }

    @Test
    public void toggleChangesOnlyOnPressEdges() {
        TemperatureAnalysisKey state = new TemperatureAnalysisKey();

        assertTrue(state.isVisible(AnalysisDisplayMode.TOGGLE, true));
        assertTrue(state.isVisible(AnalysisDisplayMode.TOGGLE, true));
        assertTrue(state.isVisible(AnalysisDisplayMode.TOGGLE, false));
        assertFalse(state.isVisible(AnalysisDisplayMode.TOGGLE, true));
    }

    @Test
    public void alwaysIgnoresTheCurrentKeyState() {
        TemperatureAnalysisKey state = new TemperatureAnalysisKey();

        assertTrue(state.isVisible(AnalysisDisplayMode.ALWAYS, false));
        assertTrue(state.isVisible(AnalysisDisplayMode.ALWAYS, true));
    }

    @Test
    public void resetClearsTheRememberedEdgeAndToggleState() {
        TemperatureAnalysisKey state = new TemperatureAnalysisKey();
        assertTrue(state.isVisible(AnalysisDisplayMode.TOGGLE, true));

        state.reset();

        assertTrue(state.isVisible(AnalysisDisplayMode.TOGGLE, true));
        state.reset();
        assertFalse(state.isVisible(AnalysisDisplayMode.TOGGLE, false));
        assertTrue(state.isVisible(AnalysisDisplayMode.ALWAYS, false));
    }

    @Test
    public void nullModeBehavesLikeHold() {
        TemperatureAnalysisKey state = new TemperatureAnalysisKey();

        assertFalse(state.isVisible(null, false));
        assertTrue(state.isVisible(null, true));
    }

    @Test
    public void enteringToggleWhileKeyIsHeldDoesNotCreateAPressEdge() {
        TemperatureAnalysisKey state = new TemperatureAnalysisKey();
        assertTrue(state.isVisible(AnalysisDisplayMode.HOLD, true));

        assertFalse(state.isVisible(AnalysisDisplayMode.TOGGLE, true));
        assertFalse(state.isVisible(AnalysisDisplayMode.TOGGLE, false));
        assertTrue(state.isVisible(AnalysisDisplayMode.TOGGLE, true));
    }

    @Test
    public void modesContinueSamplingEdgesWithoutChangingToggleVisibility() {
        TemperatureAnalysisKey state = new TemperatureAnalysisKey();
        assertTrue(state.isVisible(AnalysisDisplayMode.TOGGLE, true));

        assertTrue(state.isVisible(AnalysisDisplayMode.ALWAYS, false));
        assertTrue(state.isVisible(AnalysisDisplayMode.TOGGLE, false));
        assertTrue(state.isVisible(AnalysisDisplayMode.HOLD, true));
        assertTrue(state.isVisible(AnalysisDisplayMode.TOGGLE, true));
    }

    @Test
    public void instancesDoNotShareToggleState() {
        TemperatureAnalysisKey first = new TemperatureAnalysisKey();
        TemperatureAnalysisKey second = new TemperatureAnalysisKey();

        assertTrue(first.isVisible(AnalysisDisplayMode.TOGGLE, true));
        assertFalse(second.isVisible(AnalysisDisplayMode.TOGGLE, false));
    }

    @Test
    public void clientHandlerLifecycleHooksAreSubscribedAndInvokeTheResetDelegate() throws Exception {
        assertSubscribed("onWorldUnload", WorldEvent.Unload.class);
        assertSubscribed("onClientDisconnect", FMLNetworkEvent.ClientDisconnectionFromServerEvent.class);
        assertSubscribed("onGuiOpen", GuiOpenEvent.class);
        assertInvokesResetDelegate("onWorldUnload");
        assertInvokesResetDelegate("onGuiOpen");
    }

    @Test
    public void lifecycleResetDelegateClearsHeldAndToggledState() {
        TemperatureAnalysisKey state = new TemperatureAnalysisKey();
        assertTrue(state.isVisible(AnalysisDisplayMode.TOGGLE, true));

        ClientEventHandler.resetAnalysisState(state);

        assertTrue(state.isVisible(AnalysisDisplayMode.TOGGLE, true));
    }

    @Test
    public void disconnectHandlerResetsHeldAndToggledState() {
        final ClientEventHandler handler = new ClientEventHandler(null, null, null, null, null);

        assertResetsAnalysisState(handler, new Runnable() {
            @Override
            public void run() {
                handler.onClientDisconnect(null);
            }
        });
    }

    private static void assertSubscribed(String name, Class<?> eventType) throws Exception {
        Method method = ClientEventHandler.class.getDeclaredMethod(name, eventType);
        assertNotNull(method.getAnnotation(SubscribeEvent.class));
    }

    private static void assertInvokesResetDelegate(final String handlerName) throws IOException {
        final boolean[] found = {false};
        ClassReader reader = new ClassReader(ClientEventHandler.class.getName());
        reader.accept(new ClassVisitor(Opcodes.ASM5) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                if (!handlerName.equals(name)) {
                    return null;
                }
                return new MethodVisitor(Opcodes.ASM5) {
                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                        if (Type.getInternalName(ClientEventHandler.class).equals(owner)
                            && "resetAnalysisState".equals(name)
                            && Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(TemperatureAnalysisKey.class)).equals(descriptor)) {
                            found[0] = true;
                        }
                    }
                };
            }
        }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        assertTrue(handlerName + " must invoke resetAnalysisState", found[0]);
    }

    private static void assertResetsAnalysisState(ClientEventHandler handler, Runnable resetAction) {
        TemperatureAnalysisKey state = analysisStateFrom(handler);
        assertTrue(state.isVisible(AnalysisDisplayMode.TOGGLE, true));

        resetAction.run();
        assertTrue(state.isVisible(AnalysisDisplayMode.TOGGLE, true));

        resetAction.run();
        assertFalse(state.isVisible(AnalysisDisplayMode.TOGGLE, false));
    }

    private static TemperatureAnalysisKey analysisStateFrom(ClientEventHandler handler) {
        try {
            java.lang.reflect.Field state = ClientEventHandler.class.getDeclaredField("analysisKeyState");
            state.setAccessible(true);
            return (TemperatureAnalysisKey)state.get(handler);
        }
        catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}
