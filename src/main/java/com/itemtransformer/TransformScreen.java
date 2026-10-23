package com.itemtransformer;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TransformScreen extends Screen {
    private static final int COL_W = 100;
    private static final int GAP = 4;
    private static final int LEFT = 20;

    private String itemId;
    private ItemStack preview = ItemStack.EMPTY;
    private TransformData data;
    private TextFieldWidget idField;
    private final List<FloatSlider> sliders = new ArrayList<>();

    public TransformScreen() {
        super(Text.literal("Item Transform Editor"));
    }

    @Override
    protected void init() {
        if (itemId == null) {
            ItemStack held = client.player.getMainHandStack();
            itemId = held.isEmpty() ? "minecraft:diamond_sword" : TransformManager.idOf(held);
        }

        int fullW = COL_W * 2 + GAP;

        idField = new TextFieldWidget(textRenderer, LEFT, 40, fullW, 18, Text.literal("Item ID"));
        idField.setMaxLength(128);
        idField.setText(itemId);
        idField.setPlaceholder(Text.literal("Item ID, e.g. minecraft:apple"));
        idField.setChangedListener(this::onIdTyped);
        addDrawableChild(idField);

        addDrawableChild(ButtonWidget.builder(Text.literal("Use held item"), b -> {
            ItemStack held = client.player.getMainHandStack();
            if (!held.isEmpty()) {
                idField.setText(TransformManager.idOf(held));
            }
        }).dimensions(LEFT, 62, fullW, 18).build());

        selectItem(itemId);

        int by = 62 + 22 + 4 * 22 + 8 + 2;
        addDrawableChild(ButtonWidget.builder(Text.literal("Reset item"), b -> {
            data.reset();
            buildSliders();
        }).dimensions(LEFT, by, COL_W, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Save & Close"), b -> close())
                .dimensions(LEFT + COL_W + GAP, by, COL_W, 20).build());
    }

    private void onIdTyped(String text) {
        Identifier id = Identifier.tryParse(text.trim());
        if (id != null && Registries.ITEM.containsId(id)) {
            selectItem(id.toString());
        }
    }

    private void selectItem(String id) {
        itemId = id;
        preview = new ItemStack(Registries.ITEM.get(Identifier.of(id)));
        data = TransformManager.getOrCreate(id);
        buildSliders();
    }

    private void buildSliders() {
        for (FloatSlider s : sliders) remove(s);
        sliders.clear();
        int y = 62 + 22;
        int x2 = LEFT + COL_W + GAP;
        addSlider(LEFT, y,       "Scale",        0.1f, 5f,    data.scale,       v -> data.scale = v);
        addSlider(x2,   y,       "Ground Size",  0.1f, 5f,    data.groundScale, v -> data.groundScale = v);
        addSlider(LEFT, y + 22,  "Pos X",       -2f,   2f,    data.px,          v -> data.px = v);
        addSlider(x2,   y + 22,  "Rot X",     -180f, 180f,    data.rx,          v -> data.rx = v);
        addSlider(LEFT, y + 44,  "Pos Y",       -2f,   2f,    data.py,          v -> data.py = v);
        addSlider(x2,   y + 44,  "Rot Y",     -180f, 180f,    data.ry,          v -> data.ry = v);
        addSlider(LEFT, y + 66,  "Pos Z",       -2f,   2f,    data.pz,          v -> data.pz = v);
        addSlider(x2,   y + 66,  "Rot Z",     -180f, 180f,    data.rz,          v -> data.rz = v);
    }

    private void addSlider(int x, int y, String label, float min, float max, float value, Consumer<Float> setter) {
        FloatSlider s = new FloatSlider(x, y, COL_W, 20, label, min, max, value, setter);
        sliders.add(s);
        addDrawableChild(s);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("Item Transform Editor"), width / 2, 12, 0xFFFFFFFF);
        ctx.drawTextWithShadow(textRenderer, Text.literal("Item"), LEFT, 29, 0xFFAAAAAA);
        ctx.drawTextWithShadow(textRenderer,
                Text.literal("Applies to held item (1st & 3rd person)"), LEFT, height - 22, 0xFF888888);
        ctx.drawTextWithShadow(textRenderer,
                Text.literal("Ground Size = dropped item size"), LEFT, height - 12, 0xFF888888);

        // Preview panel
        int cx = LEFT + COL_W * 2 + GAP + 40 + (width - (LEFT + COL_W * 2 + GAP + 40)) / 2;
        int cy = height / 2;
        int half = Math.min(100, Math.max(60, (width - (LEFT + COL_W * 2 + GAP + 60)) / 2 - 10));
        ctx.fill(cx - half - 1, cy - half - 1, cx + half + 1, cy + half + 1, 0xFF555555);
        ctx.fill(cx - half, cy - half, cx + half, cy + half, 0xCC101018);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("Preview"), cx, cy - half + 4, 0xFFAAAAAA);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("Dropped"), cx, cy + half - 40, 0xFFAAAAAA);

        ctx.enableScissor(cx - half, cy - half, cx + half, cy + half);

        // Main preview (GUI is 2D in 1.21.6+, so X/Y rotation is shown as a perspective squash)
        var m = ctx.getMatrices();
        m.pushMatrix();
        m.translate(cx + data.px * 30f, cy - 12 - data.py * 30f);
        m.rotate((float) Math.toRadians(data.rz));
        float sx = (float) Math.cos(Math.toRadians(data.ry));
        float sy = (float) Math.cos(Math.toRadians(data.rx));
        float s = 4.5f * data.scale;
        m.scale(s * sx, s * sy);
        ctx.drawItem(preview, -8, -8);
        m.popMatrix();

        // Ground-size preview
        m.pushMatrix();
        m.translate(cx, cy + half - 18);
        float gs = 1.5f * data.groundScale;
        m.scale(gs, gs);
        ctx.drawItem(preview, -8, -8);
        m.popMatrix();

        ctx.disableScissor();
    }

    @Override
    public void close() {
        TransformManager.save();
        super.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static class FloatSlider extends SliderWidget {
        private final String label;
        private final float min, max;
        private final Consumer<Float> setter;

        FloatSlider(int x, int y, int w, int h, String label, float min, float max, float value, Consumer<Float> setter) {
            super(x, y, w, h, Text.empty(), (value - min) / (max - min));
            this.label = label;
            this.min = min;
            this.max = max;
            this.setter = setter;
            updateMessage();
        }

        private float current() {
            float v = (float) (min + this.value * (max - min));
            return Math.round(v * 100f) / 100f;
        }

        @Override
        protected void updateMessage() {
            setMessage(Text.literal(label + ": " + String.format("%.2f", current())));
        }

        @Override
        protected void applyValue() {
            setter.accept(current());
        }
    }
}
