package dev.rlcraftceaddons.sdtemperaturehud.client.editor;

final class HudEditorCloseController {
    private boolean closed;

    void close(
        HudEditorModel model,
        SaveOperation saveOperation,
        FailureHandler failureHandler
    ) {
        if (saveOperation == null || failureHandler == null) {
            throw new IllegalArgumentException("close operations must not be null");
        }
        if (model == null) {
            this.closed = true;
            return;
        }
        if (this.closed) {
            model.clearTransientState();
            return;
        }
        this.closed = true;
        try {
            saveOperation.save(model);
        } catch (RuntimeException exception) {
            failureHandler.onFailure(exception);
        } finally {
            model.clearTransientState();
        }
    }

    interface SaveOperation {
        void save(HudEditorModel model);
    }

    interface FailureHandler {
        void onFailure(RuntimeException exception);
    }
}
