package dev.rlcraftceaddons.sdtemperaturehud.client.editor;

import dev.rlcraftceaddons.sdtemperaturehud.client.ClientEventHandler;
import java.io.IOException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HudEditorLauncherPolicyTest {
    @Test
    public void requestIsOneShotAndOpensOnlyWhenClientIsUsable() {
        HudEditorLauncher.PendingRequest pending = new HudEditorLauncher.PendingRequest();

        assertEquals(HudEditorLauncher.Decision.NONE, pending.consume(true));
        pending.request();
        pending.request();
        assertTrue(pending.isPending());
        assertEquals(HudEditorLauncher.Decision.OPEN, pending.consume(true));
        assertFalse(pending.isPending());
        assertEquals(HudEditorLauncher.Decision.NONE, pending.consume(true));
    }

    @Test
    public void invalidClientCancelsWithoutLeavingAStuckRequest() {
        HudEditorLauncher.PendingRequest pending = new HudEditorLauncher.PendingRequest();

        pending.request();
        assertEquals(HudEditorLauncher.Decision.CANCEL, pending.consume(false));
        assertFalse(pending.isPending());
        assertEquals(HudEditorLauncher.Decision.NONE, pending.consume(true));
    }

    @Test
    public void explicitLifecycleCancellationPreventsALaterOpen() {
        HudEditorLauncher.PendingRequest pending = new HudEditorLauncher.PendingRequest();

        pending.request();
        pending.cancel();

        assertFalse(pending.isPending());
        assertEquals(HudEditorLauncher.Decision.NONE, pending.consume(true));
    }

    @Test
    public void lifecycleHooksDelegateCancellationToTheLifecycleCoordinator()
        throws IOException {
        assertInvokesLifecycle("onWorldUnload", "onWorldInvalidated");
        assertInvokesLifecycle("onClientDisconnect", "onDisconnected");
        assertInvokesLifecycle("onGuiOpen", "onScreenReplacement");
    }

    private static void assertInvokesLifecycle(
        final String handlerName,
        final String lifecycleMethod
    ) throws IOException {
        final boolean[] found = {false};
        ClassReader reader = new ClassReader(ClientEventHandler.class.getName());
        reader.accept(new ClassVisitor(Opcodes.ASM5) {
            @Override
            public MethodVisitor visitMethod(
                int access,
                String name,
                String descriptor,
                String signature,
                String[] exceptions
            ) {
                if (!handlerName.equals(name)) {
                    return null;
                }
                return new MethodVisitor(Opcodes.ASM5) {
                    @Override
                    public void visitMethodInsn(
                        int opcode,
                        String owner,
                        String name,
                        String descriptor,
                        boolean isInterface
                    ) {
                        if (opcode == Opcodes.INVOKEVIRTUAL
                            && Type.getInternalName(HudEditorLifecycleCoordinator.class).equals(owner)
                            && lifecycleMethod.equals(name)) {
                            found[0] = true;
                        }
                    }
                };
            }
        }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        assertTrue(handlerName + " must delegate to " + lifecycleMethod, found[0]);
    }

}
