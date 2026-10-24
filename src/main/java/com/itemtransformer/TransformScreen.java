package com.itemtransformer;
 
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
 
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
 
public class TransformScreen extends Screen {
    // Colors (ARGB)
    private static final int C_BORDER   = 0xFF3A4052;
    private static final int C_WINDOW   = 0xF0161922;
    private static final int C_HEADER   = 0xFF232733;
    private static final int C_GOLD     = 0xFFE0A526;
    private static final int C_LABEL    = 0xFF8A90A0;
    private static final int C_LINE     = 0xFF303544;
    private static final int C_WHITE    = 0xFFFFFFFF;
    private static final int C_RED      = 0xFFB04A4A;
    private static final int C_GREEN    = 0xFF4AA05A;
    private static final int C_BLUE     = 0xFF4A74B8;
    private static final int C_PURPLE   = 0xFF8A62C8;
    private static final int C_ORANGE   = 0xFFC8962E;
 
    private int wx, wy, ww, wh;   // window
    private int cx0, cw;          // control column
    private int px, pw;           // preview column
 
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
 
        ww = Math.min(400, width - 10);
        wh = 206;
        wx = (width - ww) / 2;
        wy = Math.max(2, (height - wh) / 2);
        cx0 = wx + 12;
        cw = Math.max(150, (ww - 36) * 54 / 100);
        px = cx0 + cw + 12;
        pw = wx + ww - 12 - px;
 
        int y0 = wy + 34;
        idField = new TextFieldWidget(textRenderer, cx0, y0 + 10, cw - 54, 18, Text.literal("Item ID"));
        idField.setMaxLength(128);
        idField.setText(itemId);
        idField.setPlaceholder(Text.literal("minecraft:apple"));
        idField.setChangedListener(this::onIdTyped);
        addDrawableChild(idField);
 
        addDrawableChild(ButtonWidget.builder(Text.literal("Held"), b -> {
            ItemStack held = client.player.getMainHandStack();
            if (!held.isEmpty()) idField.setText(TransformManager.idOf(held));
        }).dimensions(cx0 + cw - 50, y0 + 10, 50, 18).build());
 
        selectItem(itemId);
 
        int half = (cw - 4) / 2;
        int y4 = wy + 164;
        addDrawableChild(ButtonWidget.builder(Text.literal("Reset item"), b -> {
            data.reset();
            buildSliders();
        }).dimensions(cx0, y4, half, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Save & Close"), b -> close())
                .dimensions(cx0 + half + 4, y4, half, 20).build());
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
 
        int half = (cw - 4) / 2;
        int third = (cw - 8) / 3;
        int y1 = wy + 68 + 10;
        int y2 = wy + 100 + 10;
        int y3 = wy + 132 + 10;
 
        addSlider(cx0,            y1, half, "Scale",  0.1f, 5f, data.scale,       2, C_ORANGE, v -> data.scale = v);
        addSlider(cx0 + half + 4, y1, half, "Ground", 0.1f, 5f, data.groundScale, 2, C_PURPLE, v -> data.groundScale = v);
 
        addSlider(cx0,                    y2, third, "X", -2f, 2f, data.px, 2, C_RED,   v -> data.px = v);
        addSlider(cx0 + (third + 4),      y2, third, "Y", -2f, 2f, data.py, 2, C_GREEN, v -> data.py = v);
        addSlider(cx0 + (third + 4) * 2,  y2, third, "Z", -2f, 2f, data.pz, 2, C_BLUE,  v -> data.pz = v);
 
        addSlider(cx0,                    y3, third, "X", -180f, 180f, data.rx, 0, C_RED,   v -> data.rx = v);
        addSlider(cx0 + (third + 4),      y3, third, "Y", -180f, 180f, data.ry, 0, C_GREEN, v -> data.ry = v);
        addSlider(cx0 + (third + 4) * 2,  y3, third, "Z", -180f, 180f, data.rz, 0, C_BLUE,  v -> data.rz = v);
    }
 
    private void addSlider(int x, int y, int w, String label, float min, float max, float value,
                           int decimals, int accent, Consumer<Float> setter) {
        FloatSlider s = new FloatSlider(textRenderer, x, y, w, 16, label, min, max, value, decimals, accent, setter);
        sliders.add(s);
        addDrawableChild(s);
    }
 
    // ---------------------------------------------------------------- drawing
 
    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.renderBackground(ctx, mouseX, mouseY, delta);
        drawUi(ctx);
    }
 
    private void drawUi(DrawContext ctx) {
        // window
        ctx.fill(wx - 1, wy - 1, wx + ww + 1, wy + wh + 1, C_BORDER);
        ctx.fill(wx, wy, wx + ww, wy + wh, C_WINDOW);
 
        // header
        ctx.fill(wx, wy, wx + ww, wy + 26, C_HEADER);
        ctx.fill(wx, wy + 26, wx + ww, wy + 27, C_GOLD);
        ctx.drawTextWithShadow(textRenderer, Text.literal("ITEM TRANSFORMER"), wx + 12, wy + 9, C_GOLD);
        Text name = preview.isEmpty() ? Text.literal(itemId) : preview.getName();
        ctx.drawTextWithShadow(textRenderer, name, wx + ww - 12 - textRenderer.getWidth(name), wy + 9, C_WHITE);
 
        // section labels
        section(ctx, "ITEM", wy + 34);
        section(ctx, "SIZE", wy + 68);
        section(ctx, "POSITION", wy + 100);
        section(ctx, "ROTATION", wy + 132);
 
        // preview cards
        int py0 = wy + 34;
        int h1 = 98;
        int py2 = py0 + h1 + 6;
        int h2 = wy + wh - 12 - py2;
        drawHandCard(ctx, px, py0, pw, h1);
        drawGroundCard(ctx, px, py2, pw, h2);
 
        // footer
        ctx.drawTextWithShadow(textRenderer,
                Text.literal("Hand: 1st + 3rd person   |   Ground = dropped item"),
                wx + 12, wy + wh - 11, 0xFF5F6575);
    }
 
    private void section(DrawContext ctx, String label, int y) {
        ctx.drawTextWithShadow(textRenderer, Text.literal(label), cx0, y, C_LABEL);
        int tw = textRenderer.getWidth(label);
        ctx.fill(cx0 + tw + 5, y + 4, cx0 + cw, y + 5, C_LINE);
    }
 
    private void drawHandCard(DrawContext ctx, int x, int y, int w, int h) {
        ctx.fill(x - 1, y - 1, x + w + 1, y + h + 1, C_BORDER);
        for (int yy = 0; yy < h; yy += 8) {
            for (int xx = 0; xx < w; xx += 8) {
                boolean a = (((xx >> 3) + (yy >> 3)) & 1) == 0;
                ctx.fill(x + xx, y + yy, Math.min(x + xx + 8, x + w), Math.min(y + yy + 8, y + h),
                        a ? 0xFF1E222D : 0xFF232836);
            }
        }
        ctx.drawTextWithShadow(textRenderer, Text.literal("IN HAND"), x + 5, y + 5, C_LABEL);
 
        ctx.enableScissor(x, y, x + w, y + h);
        var m = ctx.getMatrices();
        m.pushMatrix();
        m.translate(x + w / 2f + data.px * 30f, y + h / 2f + 6 - data.py * 30f);
        m.rotate((float) Math.toRadians(data.rz));
        float sx = (float) Math.cos(Math.toRadians(data.ry));
        float sy = (float) Math.cos(Math.toRadians(data.rx));
        float s = 4f * data.scale;
        m.scale(s * sx, s * sy);
        ctx.drawItem(preview, -8, -8);
        m.popMatrix();
        ctx.disableScissor();
    }
 
    private void drawGroundCard(DrawContext ctx, int x, int y, int w, int h) {
        ctx.fill(x - 1, y - 1, x + w + 1, y + h + 1, C_BORDER);
        ctx.fill(x, y, x + w, y + h, 0xFF1B2230);
        int groundTop = y + h - 14;
        ctx.fill(x, groundTop, x + w, y + h, 0xFF5B4630);
        ctx.fill(x, groundTop, x + w, groundTop + 3, 0xFF4C7A3A);
        ctx.drawTextWithShadow(textRenderer, Text.literal("ON GROUND"), x + 5, y + 5, C_LABEL);
 
        ctx.enableScissor(x, y, x + w, y + h);
        var m = ctx.getMatrices();
        m.pushMatrix();
        float gs = 1.2f * data.groundScale;
        m.translate(x + w / 2f, groundTop - 8f * gs + 2);
        m.scale(gs, gs);
        ctx.drawItem(preview, -8, -8);
        m.popMatrix();
        ctx.disableScissor();
    }
 
    // ---------------------------------------------------------------- misc
 
    @Override
    public void close() {
        TransformManager.save();
        super.close();
    }
 
    @Override
    public boolean shouldPause() {
        return false;
    }
 
    /** Slider with a custom flat look and a colored fill bar. */
    private static class FloatSlider extends SliderWidget {
        private final TextRenderer font;
        private final String label;
        private final float min, max;
        private final int decimals;
        private final int accent;
        private final Consumer<Float> setter;
 
        FloatSlider(TextRenderer font, int x, int y, int w, int h, String label, float min, float max,
                    float value, int decimals, int accent, Consumer<Float> setter) {
            super(x, y, w, h, Text.empty(), (value - min) / (max - min));
            this.font = font;
            this.label = label;
            this.min = min;
            this.max = max;
            this.decimals = decimals;
            this.accent = accent;
            this.setter = setter;
            updateMessage();
        }
 
        private float current() {
            float v = (float) (min + this.value * (max - min));
            float f = (float) Math.pow(10, decimals);
            return Math.round(v * f) / f;
        }
 
        @Override
        protected void updateMessage() {
            setMessage(Text.literal(label + ": " + String.format("%." + decimals + "f", current())));
        }
 
        @Override
        protected void applyValue() {
            setter.accept(current());
        }
 
        @Override
        public void renderWidget(DrawContext ctx, int mouseX, int mouseY, float delta) {
            int x = getX(), y = getY(), w = getWidth(), h = getHeight();
            boolean hot = isHovered() || isFocused();
            ctx.fill(x - 1, y - 1, x + w + 1, y + h + 1, hot ? 0xFF6A7390 : 0xFF3A4052);
            ctx.fill(x, y, x + w, y + h, hot ? 0xFF2C3140 : 0xFF222633);
            int fillW = (int) (this.value * (w - 2));
            ctx.fill(x + 1, y + 1, x + 1 + fillW, y + h - 1, accent);
            ctx.fill(x + fillW, y, x + fillW + 2, y + h, 0xFFFFFFFF);
            ctx.drawCenteredTextWithShadow(font, getMessage(), x + w / 2, y + (h - 8) / 2, 0xFFFFFFFF);
        }
    }
}
 
