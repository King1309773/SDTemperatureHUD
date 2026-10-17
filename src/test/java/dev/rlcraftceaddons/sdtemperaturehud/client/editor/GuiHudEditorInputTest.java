package dev.rlcraftceaddons.sdtemperaturehud.client.editor;

import java.io.IOException;
import net.minecraft.client.gui.GuiScreen;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class GuiHudEditorInputTest {
    @Test
    public void nonzeroWheelBranchMarksForgeMouseEventHandled() throws IOException {
        final boolean[] readWheel = {false};
        final boolean[] guardedNonzero = {false};
        final boolean[] markedHandled = {false};
        ClassReader reader = new ClassReader(GuiHudEditor.class.getName());

        reader.accept(new ClassVisitor(Opcodes.ASM5) {
            @Override
            public MethodVisitor visitMethod(
                int access,
                String name,
                String descriptor,
                String signature,
                String[] exceptions
            ) {
                if (!"handleMouseInput".equals(name) || !"()V".equals(descriptor)) {
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
                        if (opcode == Opcodes.INVOKESTATIC
                            && "org/lwjgl/input/Mouse".equals(owner)
                            && "getEventDWheel".equals(name)
                            && "()I".equals(descriptor)) {
                            readWheel[0] = true;
                        }
                    }

                    @Override
                    public void visitJumpInsn(int opcode, org.objectweb.asm.Label label) {
                        if (readWheel[0] && opcode == Opcodes.IFEQ) {
                            guardedNonzero[0] = true;
                        }
                    }

                    @Override
                    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                        if (guardedNonzero[0]
                            && opcode == Opcodes.PUTFIELD
                            && "mouseHandled".equals(name)
                            && "Z".equals(descriptor)
                            && (Type.getInternalName(GuiHudEditor.class).equals(owner)
                                || Type.getInternalName(GuiScreen.class).equals(owner))) {
                            markedHandled[0] = true;
                        }
                    }
                };
            }
        }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

        assertTrue("handleMouseInput must read the wheel delta", readWheel[0]);
        assertTrue("wheel handling must be guarded by a nonzero check", guardedNonzero[0]);
        assertTrue("the handled wheel branch must mark GuiScreen.mouseHandled", markedHandled[0]);
    }

    @Test
    public void nullAnalysisMeasurementUsesTheSelectableEditorPlaceholder() throws IOException {
        final boolean[] readAnalysisPanel = {false};
        final boolean[] guardedNullPanel = {false};
        final boolean[] drewPlaceholder = {false};
        ClassReader reader = new ClassReader(GuiHudEditor.class.getName());

        reader.accept(new ClassVisitor(Opcodes.ASM5) {
            @Override
            public MethodVisitor visitMethod(
                int access,
                String name,
                String descriptor,
                String signature,
                String[] exceptions
            ) {
                if (!"drawScreen".equals(name) || !"(IIF)V".equals(descriptor)) {
                    return null;
                }
                return new MethodVisitor(Opcodes.ASM5) {
                    @Override
                    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                        if (opcode == Opcodes.GETFIELD
                            && Type.getInternalName(GuiHudEditor.class).equals(owner)
                            && "analysisPanel".equals(name)) {
                            readAnalysisPanel[0] = true;
                        }
                    }

                    @Override
                    public void visitJumpInsn(int opcode, org.objectweb.asm.Label label) {
                        if (readAnalysisPanel[0] && opcode == Opcodes.IFNONNULL) {
                            guardedNullPanel[0] = true;
                        }
                    }

                    @Override
                    public void visitMethodInsn(
                        int opcode,
                        String owner,
                        String name,
                        String descriptor,
                        boolean isInterface
                    ) {
                        if (guardedNullPanel[0]
                            && opcode == Opcodes.INVOKESPECIAL
                            && Type.getInternalName(GuiHudEditor.class).equals(owner)
                            && "drawEmptyAnalysisPreview".equals(name)
                            && "()V".equals(descriptor)) {
                            drewPlaceholder[0] = true;
                        }
                    }
                };
            }
        }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

        assertTrue("drawScreen must inspect the measured analysis panel", readAnalysisPanel[0]);
        assertTrue("the placeholder must be guarded by a null panel check", guardedNullPanel[0]);
        assertTrue("a null analysis measurement must draw the editor placeholder", drewPlaceholder[0]);
    }
}
