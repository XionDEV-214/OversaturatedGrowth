package com.eisiadev.oversaturatedgrowth.accessory.gui;

import com.eisiadev.oversaturatedgrowth.accessory.AccessoryBag;
import com.eisiadev.oversaturatedgrowth.accessory.AccessoryData;
import com.eisiadev.oversaturatedgrowth.accessory.AccessoryEffects;
import com.eisiadev.oversaturatedgrowth.accessory.AccessoryItem;
import com.eisiadev.oversaturatedgrowth.accessory.AccessoryStat;
import com.eisiadev.oversaturatedgrowth.accessory.AccessoryTier;
import com.eisiadev.oversaturatedgrowth.accessory.AccessoryTotals;
import com.eisiadev.oversaturatedgrowth.accessory.Awakening;
import com.eisiadev.oversaturatedgrowth.accessory.AwakeningCost;
import com.eisiadev.oversaturatedgrowth.accessory.Reroll;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.widget.Interactable;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widget.Widget;
import brachy.modularui.widgets.SlotGroupWidget;
import brachy.modularui.widgets.slot.ItemSlot;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/** Client-only widgets of the Accessory Bag screen. */
public final class AccessoryClientUI {

    private static final int W = AccessoryGui.WIDTH;
    private static final int H = AccessoryGui.HEIGHT;

    private static final int LEFT_W = AccessoryGui.SUMMARY_W;
    private static final int LEFT_H = H;
    private static final int LEFT_X = 0;
    private static final int RIGHT_W = AccessoryGui.SIDE_W;
    private static final int RIGHT_H = AccessoryGui.SIDE_HEIGHT;
    private static final int RIGHT_X = AccessoryGui.SIDE_X;
    private static final int PAD = 6;
    private static final int INNER_W = RIGHT_W - PAD * 2;
    private static final int BUTTON_Y = RIGHT_H - 20;

    private static final int TITLE = 0xFF404040;
    private static final int BODY = 0xFF303030;
    private static final int MUTED = 0xFF6E6E6E;
    private static final int GOOD = 0xFF1E7A1E;
    private static final int BAD = 0xFFB02020;
    private static final int PLATE = 0xFFC6C6C6;
    private static final int FRAME = 0xFF373737;

    private static final int TAB_AWAKEN = AccessoryGui.TAB_AWAKEN;
    private static final int TAB_REROLL = AccessoryGui.TAB_REROLL;

    private AccessoryClientUI() {}

    /** Per-screen client state: active tab and the reroll locks. */
    private static final class State {
        int tab = TAB_AWAKEN;
        int lockMask = 0;
        long rerollKey = Long.MIN_VALUE;

        /** Locks belong to one accessory: they are kept across rerolls (colours survive a reroll) but cleared on a swap. */
        int locks(AccessoryGui gui) {
            ItemStack stack = gui.reroll().getStackInSlot(0);
            AccessoryData d = AccessoryData.read(stack);
            long key = d == null ? Long.MIN_VALUE
                    : ((long) stack.getItem().hashCode() << 32) ^ ((long) d.gemColor() << 8) ^ d.bandColor() ^ ((long) d.tier().ordinal() << 56);
            if (key != rerollKey) {
                rerollKey = key;
                lockMask = 0;
            }
            return d == null ? 0 : Reroll.sanitize(lockMask, d);
        }
    }

    public static void build(ModularPanel<?> panel, AccessoryGui gui) {
        State state = new State();

        // One window: a single plate behind the summary, the bag and the side tabs. It is the panel's own
        // background (always drawn before any child), and the bag slots live in their own group like the
        // inventory and the side slots do.
        panel.disableThemeBackground(true);
        panel.background((context, x, y, w, h, theme) -> {
            GuiGraphics g = context.getGraphics();
            g.pose().pushPose();
            g.pose().translate(x, y, 0);
            plate(g, w, h, false, false);
            text(g, tr("gui.oversaturatedgrowth.accessory.title"), AccessoryGui.MAIN_X + 8, 6, 1f, TITLE);
            text(g, Component.translatable("container.inventory").getString(), AccessoryGui.INVENTORY_X, AccessoryGui.INVENTORY_Y - 11, 1f, TITLE);
            g.pose().popPose();
        });

        Side bag = new Side(false, false);
        bag.pos(AccessoryGui.BAG_X, AccessoryGui.BAG_Y).size(9 * 18, 5 * 18);
        for (int i = 0; i < AccessoryBag.SIZE; i++) {
            bag.child(new AccessorySlot(gui, state, AccessoryGui.ZONE_BAG, i).syncHandler(AccessoryGui.SLOT_BAG, i)
                    .pos((i % 9) * 18, (i / 9) * 18));
        }
        panel.child(bag);
        panel.child(SlotGroupWidget.playerInventory(false,
                        (index, slot) -> new AccessorySlot(gui, state, AccessoryGui.ZONE_PLAYER, index))
                .pos(AccessoryGui.INVENTORY_X, AccessoryGui.INVENTORY_Y));

        panel.child(summary(gui));
        panel.child(sidePanel(gui, state));
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Left: summed stats and what they actually do right now
    // ---------------------------------------------------------------------------------------------------------------

    private static ParentWidget<?> summary(AccessoryGui gui) {
        Side side = new Side(false, true);
        side.pos(LEFT_X, 0).size(LEFT_W, LEFT_H);
        side.child(new Canvas((g, w, h) -> {
            AccessoryTotals t = AccessoryTotals.of(gui.bagItems());
            text(g, tr("gui.oversaturatedgrowth.accessory.summary"), PAD, 6, 1f, TITLE);
            AccessoryStat[] stats = AccessoryStat.values();
            for (int i = 0; i < stats.length; i++) {
                int y = 20 + i * 34;
                AccessoryStat stat = stats[i];
                int color = 0xFF000000 | stat.baseColor();
                g.fill(PAD, y, PAD + 2, y + 28, color);
                text(g, fit(stat.displayName().getString(), w - PAD * 2 - 5, 0.8f), PAD + 5, y, 0.8f, BODY);
                String effect = effectLine(gui.player(), stat, t);
                boolean active = total(stat, t) > 0;
                wrap(g, effect, PAD + 5, y + 9, w - PAD * 2 - 5, 0.65f, active ? darker(color) : MUTED, 3);
            }
            text(g, Component.translatable("gui.oversaturatedgrowth.accessory.slots", t.slotsUsed(), AccessoryBag.SIZE).getString(),
                    PAD, h - 12, 0.75f, TITLE);
        }).pos(0, 0).size(LEFT_W, LEFT_H));
        return side;
    }

    private static double total(AccessoryStat stat, AccessoryTotals t) {
        return switch (stat) {
            case MAX_CALORIES -> t.maxCalories();
            case DIGESTION -> t.digestion();
            case MOVEMENT -> t.movementPct();
            case HEALTH -> t.healthHp();
            case ABSORPTION -> t.absorptionPct();
        };
    }

    /** One plain-language line per stat, using the values the server actually applied where the client can see them. */
    private static String effectLine(Player player, AccessoryStat stat, AccessoryTotals t) {
        String key = "gui.oversaturatedgrowth.accessory.effect." + stat.id;
        return switch (stat) {
            case MAX_CALORIES -> Component.translatable(key, t.bonusCalories()).getString();
            case DIGESTION -> Component.translatable(key, fmt("%.1f", t.digestShare(0.05) * 100.0)).getString();
            case MOVEMENT -> Component.translatable(key, fmt("%.0f", t.movementReduction() * 100.0),
                    fmt("%.1f", modifier(player, Attributes.MOVEMENT_SPEED, AccessoryEffects.SPEED_ID) * 100.0)).getString();
            case HEALTH -> Component.translatable(key,
                    fmt("%.1f", modifier(player, Attributes.MAX_HEALTH, AccessoryEffects.HEALTH_ID)),
                    fmt("%.1f", t.healthHp())).getString();
            case ABSORPTION -> Component.translatable(key, fmt("%.2f", t.absorptionMultiplier())).getString();
        };
    }

    private static double modifier(Player player, net.minecraft.world.entity.ai.attributes.Attribute attribute, UUID id) {
        AttributeInstance inst = player.getAttribute(attribute);
        if (inst == null) return 0;
        AttributeModifier m = inst.getModifier(id);
        return m == null ? 0 : m.getAmount();
    }

    private static String fmt(String pattern, double v) {
        return String.format(Locale.ROOT, pattern, v);
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Right: Awakening / Reroll tabs
    // ---------------------------------------------------------------------------------------------------------------

    private static ParentWidget<?> sidePanel(AccessoryGui gui, State state) {
        Side side = new Side(true, false);
        side.pos(RIGHT_X, 0).size(RIGHT_W, RIGHT_H);

        int tabW = (INNER_W - 2) / 2;
        side.child(new Tab(state, TAB_AWAKEN, "gui.oversaturatedgrowth.accessory.awaken").pos(PAD, 4).size(tabW, 14));
        side.child(new Tab(state, TAB_REROLL, "gui.oversaturatedgrowth.accessory.reroll").pos(PAD + tabW + 2, 4).size(tabW, 14));

        // Canvases first (and never hoverable) so they can never sit on top of a slot or button.
        side.child(new Canvas((g, w, h) -> paintAwaken(g, gui)).pos(0, 0).size(RIGHT_W, RIGHT_H)
                .setEnabledIf(c -> state.tab == TAB_AWAKEN));
        side.child(new Canvas((g, w, h) -> paintReroll(g, gui, state)).pos(0, 0).size(RIGHT_W, RIGHT_H)
                .setEnabledIf(c -> state.tab == TAB_REROLL));

        // Awakening widgets
        for (int i = 0; i < Awakening.INPUTS; i++) {
            side.child(new AccessorySlot(gui, state, AccessoryGui.ZONE_INPUT, i).syncHandler(AccessoryGui.SLOT_IN, i)
                    .pos(PAD + 1 + i * 18, 24)
                    .setEnabledIf(s -> state.tab == TAB_AWAKEN));
        }
        side.child(new AccessorySlot(gui, state, AccessoryGui.ZONE_OUTPUT, 0).syncHandler(AccessoryGui.SLOT_OUT, 0)
                .pos(RIGHT_W - PAD - 19, 24)
                .setEnabledIf(s -> state.tab == TAB_AWAKEN));
        side.child(new ActionButton(
                () -> Component.translatable("gui.oversaturatedgrowth.accessory.awaken.button"),
                () -> Awakening.check(gui.player(), gui.inputs(), gui.output()) == Awakening.Check.OK,
                () -> gui.sync().callSyncedAction(AccessoryGui.ACTION_AWAKEN))
                .pos(PAD, BUTTON_Y).size(INNER_W, 16)
                .setEnabledIf(b -> state.tab == TAB_AWAKEN));

        // Reroll widgets
        side.child(new AccessorySlot(gui, state, AccessoryGui.ZONE_REROLL, 0).syncHandler(AccessoryGui.SLOT_REROLL, 0)
                .pos(PAD + 1, 24)
                .setEnabledIf(s -> state.tab == TAB_REROLL));
        for (int i = 0; i < AccessoryTier.HELIX.maxStats(); i++) {
            int line = i;
            side.child(new LockToggle(gui, state, line).pos(PAD, REROLL_LINES_Y + line * LINE_H).size(9, 9)
                    .setEnabledIf(l -> state.tab == TAB_REROLL && line < rerollLines(gui)));
        }
        side.child(new ActionButton(
                () -> Component.translatable("gui.oversaturatedgrowth.accessory.reroll.button"),
                () -> Reroll.check(gui.player(), gui.reroll(), state.locks(gui)) == Reroll.Check.OK,
                () -> {
                    int mask = state.locks(gui);
                    gui.sync().callSyncedAction(AccessoryGui.ACTION_REROLL, buf -> buf.writeVarInt(mask));
                })
                .pos(PAD, BUTTON_Y).size(INNER_W, 16)
                .setEnabledIf(b -> state.tab == TAB_REROLL));

        // Material tooltips: invisible hover areas over the cost rows.
        for (int i = 0; i < 4; i++) {
            int idx = i;
            side.child(new CostHover(gui, () -> {
                AccessoryTier target = Awakening.target(gui.inputs());
                if (target == null) return null;
                List<AwakeningCost.Req> reqs = AwakeningCost.forTarget(target);
                return idx < reqs.size() ? reqs.get(idx) : null;
            }).pos(PAD, AWAKEN_COST_Y + i * 14).size(INNER_W, 13).setEnabledIf(c -> state.tab == TAB_AWAKEN && c.req() != null));
        }
        for (int lines = 1; lines <= AccessoryTier.HELIX.maxStats(); lines++) {
            for (int i = 0; i < 2; i++) {
                int idx = i;
                int n = lines;
                side.child(new CostHover(gui, () -> {
                    AccessoryData d = AccessoryData.read(gui.reroll().getStackInSlot(0));
                    if (d == null) return null;
                    return AwakeningCost.forReroll(d.tier(), Integer.bitCount(state.locks(gui))).get(idx);
                }).pos(PAD + i * (INNER_W / 2), REROLL_LINES_Y + n * LINE_H + 4).size(INNER_W / 2 - 2, 13)
                        .setEnabledIf(c -> state.tab == TAB_REROLL && rerollLines(gui) == n));
            }
        }
        return side;
    }

    private static final int AWAKEN_COST_Y = 58;

    private static final int REROLL_LINES_Y = 47;
    private static final int LINE_H = 11;

    private static int rerollLines(AccessoryGui gui) {
        AccessoryData d = AccessoryData.read(gui.reroll().getStackInSlot(0));
        return d == null ? 0 : d.entries().size();
    }

    private static void paintAwaken(GuiGraphics g, AccessoryGui gui) {
        text(g, "→", PAD + 58, 29, 1.2f, TITLE);

        AccessoryTier target = Awakening.target(gui.inputs());
        int y = 46;
        if (target == null) {
            wrap(g, tr("gui.oversaturatedgrowth.accessory.awaken.hint"), PAD, y, INNER_W, 0.75f, MUTED, 6);
            return;
        }
        String label = tr("gui.oversaturatedgrowth.accessory.awaken.target") + " ";
        text(g, label, PAD, y, 0.8f, TITLE);
        int labelW = (int) Math.ceil(font().width(label) * 0.8f);
        text(g, fit(target.displayName().getString(), INNER_W - labelW, 0.8f), PAD + labelW, y, 0.8f, 0xFF000000 | target.color);

        y = AWAKEN_COST_Y;
        for (AwakeningCost.Req r : AwakeningCost.forTarget(target)) {
            drawCost(g, gui.player(), r, PAD, y);
            y += 14;
        }

        Awakening.Check check = Awakening.check(gui.player(), gui.inputs(), gui.output());
        wrap(g, tr("gui.oversaturatedgrowth.accessory.awaken.check." + check.name().toLowerCase(Locale.ROOT)),
                PAD, Math.max(y + 2, 118), INNER_W, 0.75f, check == Awakening.Check.OK ? GOOD : BAD, 4);
    }

    private static void paintReroll(GuiGraphics g, AccessoryGui gui, State state) {
        ItemStack stack = gui.reroll().getStackInSlot(0);
        AccessoryData d = AccessoryData.read(stack);
        if (d == null) {
            wrap(g, tr("gui.oversaturatedgrowth.accessory.reroll.hint"), PAD, 46, INNER_W, 0.75f, MUTED, 8);
            return;
        }
        int locks = state.locks(gui);
        int nameX = PAD + 22;
        text(g, fit(stack.getHoverName().getString(), RIGHT_W - nameX - PAD, 0.75f), nameX, 26, 0.75f, 0xFF000000 | d.tier().color);
        text(g, fit(Component.translatable("gui.oversaturatedgrowth.accessory.reroll.locks", Integer.bitCount(locks),
                d.entries().size() - 1).getString(), RIGHT_W - nameX - PAD, 0.7f), nameX, 35, 0.7f, MUTED);

        List<AccessoryData.Entry> entries = d.entries();
        int percentRight = RIGHT_W - PAD;
        for (int i = 0; i < entries.size(); i++) {
            AccessoryData.Entry e = entries.get(i);
            int y = REROLL_LINES_Y + i * LINE_H + 1;
            boolean locked = (locks & (1 << i)) != 0;
            String pct = "[" + e.stat().percentOf(d.tier(), e.value()) + "%]";
            int pctW = (int) Math.ceil(font().width(pct) * 0.7f);
            int textX = PAD + 12;
            String line = e.stat().format(e.value()) + " " + e.stat().displayName().getString();
            int color = 0xFF000000 | darker(e.stat().color(d.tier(), e.value()));
            if (locked) g.fill(PAD + 10, y - 1, RIGHT_W - PAD + 1, y + 7, 0x40FFCA18);
            text(g, fit(line, percentRight - pctW - 2 - textX, 0.7f), textX, y, 0.7f, color);
            text(g, pct, percentRight - pctW, y, 0.7f, 0xFF8A6A00);
        }

        int y = REROLL_LINES_Y + entries.size() * LINE_H + 4;
        List<AwakeningCost.Req> cost = AwakeningCost.forReroll(d.tier(), Integer.bitCount(locks));
        for (int i = 0; i < cost.size(); i++) drawCost(g, gui.player(), cost.get(i), PAD + i * (INNER_W / 2), y);

        Reroll.Check check = Reroll.check(gui.player(), gui.reroll(), locks);
        wrap(g, tr("gui.oversaturatedgrowth.accessory.reroll.check." + check.name().toLowerCase(Locale.ROOT)),
                PAD, y + 16, INNER_W, 0.7f, check == Reroll.Check.OK ? GOOD : BAD, 2);
    }

    private static void drawCost(GuiGraphics g, Player player, AwakeningCost.Req r, int x, int y) {
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(0.75f, 0.75f, 1f);
        g.renderItem(r.icon(), 0, 0);
        g.pose().popPose();
        int have = AwakeningCost.count(player, r.item().get());
        text(g, have + "/" + r.count(), x + 14, y + 3, 0.75f, have >= r.count() ? GOOD : BAD);
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Text helpers
    // ---------------------------------------------------------------------------------------------------------------

    private static Font font() {
        return Minecraft.getInstance().font;
    }

    private static String tr(String key) {
        return Component.translatable(key).getString();
    }

    private static void text(GuiGraphics g, String s, float x, float y, float scale, int color) {
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(scale, scale, 1f);
        g.drawString(font(), s, 0, 0, color, false);
        g.pose().popPose();
    }

    /** Cuts {@code s} so that it fits in {@code width} screen pixels at {@code scale}, adding "..." when cut. */
    private static String fit(String s, int width, float scale) {
        Font font = font();
        int max = (int) (width / scale);
        if (font.width(s) <= max) return s;
        return font.plainSubstrByWidth(s, Math.max(0, max - font.width("..."))) + "...";
    }

    /** Word-wrapped text inside {@code width} screen pixels; returns the height used. */
    private static int wrap(GuiGraphics g, String s, int x, int y, int width, float scale, int color, int maxLines) {
        Font font = font();
        List<FormattedCharSequence> lines = font.split(FormattedText.of(s), (int) (width / scale));
        int lineH = (int) Math.ceil(9 * scale);
        int n = Math.min(maxLines, lines.size());
        for (int i = 0; i < n; i++) {
            g.pose().pushPose();
            g.pose().translate(x, y + i * lineH, 0);
            g.pose().scale(scale, scale, 1f);
            g.drawString(font, lines.get(i), 0, 0, color, false);
            g.pose().popPose();
        }
        return n * lineH;
    }

    private static int darker(int argb) {
        int r = (argb >> 16 & 0xFF) * 3 / 5;
        int gr = (argb >> 8 & 0xFF) * 3 / 5;
        int b = (argb & 0xFF) * 3 / 5;
        return 0xFF000000 | r << 16 | gr << 8 | b;
    }

    private static void click() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Widgets
    // ---------------------------------------------------------------------------------------------------------------

    /**
     * Vanilla-looking beveled plate. An open edge has no border, so a side panel that overlaps the main body by a few
     * pixels covers the body's border there and the two read as one continuous window.
     */
    private static void plate(GuiGraphics g, int w, int h, boolean openLeft, boolean openRight) {
        int l = openLeft ? 0 : 1;
        int r = openRight ? w : w - 1;
        g.fill(0, 0, w, 1, FRAME);
        g.fill(0, h - 1, w, h, FRAME);
        if (!openLeft) g.fill(0, 0, 1, h, FRAME);
        if (!openRight) g.fill(w - 1, 0, w, h, FRAME);
        g.fill(l, 1, r, h - 1, 0xFFFFFFFF);
        g.fill(openLeft ? 0 : 2, 2, openRight ? w : w - 2, h - 2, PLATE);
        g.fill(openLeft ? 0 : 2, h - 2, r, h - 1, 0xFF555555);
        if (!openRight) g.fill(w - 2, 2, w - 1, h - 1, 0xFF555555);
    }

    /** Transparent group that only positions its children (the whole window shares one plate). */
    private static final class Side extends ParentWidget<Side> {

        Side(boolean openLeft, boolean openRight) {
            disableThemeBackground(true);
        }

        @Override
        public void drawBackground(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        }
    }

    /** Vanilla inventory slot look, drawn by us so it does not depend on the theme. */
    private static final IDrawable SLOT_BACKGROUND = (context, x, y, w, h, theme) -> {
        GuiGraphics g = context.getGraphics();
        g.fill(x, y, x + w, y + h, 0xFF8B8B8B);
        g.fill(x, y, x + w - 1, y + 1, 0xFF373737);
        g.fill(x, y, x + 1, y + h - 1, 0xFF373737);
        g.fill(x + 1, y + h - 1, x + w, y + h, 0xFFFFFFFF);
        g.fill(x + w - 1, y + 1, x + w, y + h, 0xFFFFFFFF);
    };

    /** Invisible hover area that names one material and how many the player has. */
    private static final class CostHover extends Widget<CostHover> {

        private final Supplier<AwakeningCost.Req> req;

        CostHover(AccessoryGui gui, Supplier<AwakeningCost.Req> req) {
            this.req = req;
            tooltipDynamic(t -> {
                AwakeningCost.Req r = req.get();
                t.clearText();
                if (r == null) return;
                ItemStack icon = r.icon();
                t.addLine(icon.getHoverName());
                int have = AwakeningCost.count(gui.player(), r.item().get());
                t.addLine(Component.translatable("gui.oversaturatedgrowth.accessory.cost", have, r.count())
                        .withStyle(have >= r.count() ? ChatFormatting.GREEN : ChatFormatting.RED));
            });
            tooltipAutoUpdate(true);
        }

        AwakeningCost.Req req() {
            return req.get();
        }
    }

    /**
     * Bag / inventory / awakening / reroll slot with the accessory click rules (see AccessoryGui#move). Accessories
     * move on a plain left click (no dragging); anything else in the player's inventory behaves like vanilla.
     */
    private static final class AccessorySlot extends ItemSlot {

        private final AccessoryGui gui;
        private final State state;
        private final int zone;
        private final int index;
        private boolean handled;

        AccessorySlot(AccessoryGui gui, State state, int zone, int index) {
            this.gui = gui;
            this.state = state;
            this.zone = zone;
            this.index = index;
            disableThemeBackground(true);
            background(SLOT_BACKGROUND);
            AccessoryTooltips.fix(this, this::hints);
        }

        private static boolean isAccessory(ItemStack stack) {
            return !stack.isEmpty() && stack.getItem() instanceof AccessoryItem && AccessoryData.read(stack) != null;
        }

        private List<Component> hints(ItemStack stack) {
            if (!isAccessory(stack)) return List.of();
            Component tab = Component.translatable(state.tab == TAB_REROLL
                    ? "gui.oversaturatedgrowth.accessory.reroll" : "gui.oversaturatedgrowth.accessory.awaken");
            String k = "gui.oversaturatedgrowth.accessory.click.";
            return switch (zone) {
                case AccessoryGui.ZONE_PLAYER -> List.of(
                        Component.translatable(k + "to_bag").withStyle(ChatFormatting.GRAY),
                        Component.translatable(k + "shift_to_tab", tab).withStyle(ChatFormatting.GRAY));
                case AccessoryGui.ZONE_BAG -> List.of(
                        Component.translatable(k + "to_inventory").withStyle(ChatFormatting.GRAY),
                        Component.translatable(k + "shift_to_tab", tab).withStyle(ChatFormatting.GRAY));
                default -> List.of(
                        Component.translatable(k + "side_to_bag").withStyle(ChatFormatting.GRAY),
                        Component.translatable(k + "shift_to_inventory").withStyle(ChatFormatting.GRAY));
            };
        }

        @Override
        public @NotNull Result onMousePressed(int button) {
            handled = false;
            if (isSynced()) {
                ItemStack stack = getSlot().getItem();
                boolean emptyCursor = gui.player().containerMenu.getCarried().isEmpty();
                if (button == 0 && emptyCursor && isAccessory(stack)) {
                    boolean shift = Interactable.hasShiftDown();
                    int tab = state.tab;
                    gui.sync().callSyncedAction(AccessoryGui.ACTION_MOVE, buf -> {
                        buf.writeVarInt(zone);
                        buf.writeVarInt(index);
                        buf.writeBoolean(shift);
                        buf.writeVarInt(tab);
                    });
                    handled = true;
                    return Result.SUCCESS;
                }
            }
            if (zone != AccessoryGui.ZONE_PLAYER) {
                // bag and side slots never take vanilla clicks
                handled = true;
                return Result.SUCCESS;
            }
            return super.onMousePressed(button);
        }

        @Override
        public boolean onMouseReleased(int button) {
            if (handled) {
                handled = false;
                return true;
            }
            return super.onMouseReleased(button);
        }

        @Override
        public void onMouseDrag(int mouseButton, double timeSinceClick, double unused) {
            if (handled) return;
            super.onMouseDrag(mouseButton, timeSinceClick, unused);
        }
    }

    private interface Painter {
        void paint(GuiGraphics g, int w, int h);
    }

    /** Pure drawing layer. Never hoverable, so it never steals clicks from slots or buttons under / over it. */
    private static final class Canvas extends Widget<Canvas> {

        private final Painter painter;

        Canvas(Painter painter) {
            this.painter = painter;
        }

        @Override
        public boolean canHover() {
            return false;
        }

        @Override
        public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
            painter.paint(context.getGraphics(), getArea().width, getArea().height);
        }
    }

    private static final class Tab extends Widget<Tab> implements Interactable {

        private final State state;
        private final int index;
        private final String key;

        Tab(State state, int index, String key) {
            this.state = state;
            this.index = index;
            this.key = key;
        }

        @Override
        public @NotNull Result onMousePressed(int button) {
            if (button != 0) return Result.IGNORE;
            if (state.tab != index) {
                state.tab = index;
                click();
            }
            return Result.SUCCESS;
        }

        @Override
        public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
            GuiGraphics g = context.getGraphics();
            int w = getArea().width;
            int h = getArea().height;
            boolean active = state.tab == index;
            boolean hover = isHovering();
            g.fill(0, 0, w, h, FRAME);
            g.fill(1, 1, w - 1, h - 1, active ? 0xFFE8E8E8 : hover ? 0xFFADADAD : 0xFF9A9A9A);
            if (active) g.fill(1, h - 2, w - 1, h - 1, 0xFFFFCA18);
            String label = fit(tr(key), w - 4, 0.8f);
            int lw = (int) Math.ceil(font().width(label) * 0.8f);
            text(g, label, (w - lw) / 2f, (h - 7) / 2f, 0.8f, active ? TITLE : 0xFF2A2A2A);
        }
    }

    private static final class LockToggle extends Widget<LockToggle> implements Interactable {

        private final AccessoryGui gui;
        private final State state;
        private final int line;

        LockToggle(AccessoryGui gui, State state, int line) {
            this.gui = gui;
            this.state = state;
            this.line = line;
            tooltipDynamic(t -> t.clearText().addLine(Component.translatable(locked()
                    ? "gui.oversaturatedgrowth.accessory.reroll.unlock" : "gui.oversaturatedgrowth.accessory.reroll.lock")));
            tooltipAutoUpdate(true);
        }

        private boolean locked() {
            return (state.locks(gui) & (1 << line)) != 0;
        }

        @Override
        public @NotNull Result onMousePressed(int button) {
            if (button != 0) return Result.IGNORE;
            state.locks(gui);
            state.lockMask ^= 1 << line;
            click();
            return Result.SUCCESS;
        }

        @Override
        public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
            GuiGraphics g = context.getGraphics();
            boolean locked = locked();
            boolean hover = isHovering();
            int body = locked ? 0xFFE0A800 : hover ? 0xFF8A8A8A : 0xFF6A6A6A;
            int dark = 0xFF2A2A2A;
            // shackle
            if (locked) {
                g.fill(2, 0, 7, 1, dark);
                g.fill(2, 0, 3, 4, dark);
                g.fill(6, 0, 7, 4, dark);
            } else {
                g.fill(4, 0, 9, 1, dark);
                g.fill(4, 0, 5, 3, dark);
                g.fill(8, 0, 9, 4, dark);
            }
            // body
            g.fill(1, 4, 8, 9, dark);
            g.fill(2, 5, 7, 8, body);
            g.fill(4, 6, 5, 7, dark);
        }
    }

    private static final class ActionButton extends Widget<ActionButton> implements Interactable {

        private final Supplier<Component> label;
        private final BooleanSupplier ready;
        private final Runnable action;
        private boolean pressed;

        ActionButton(Supplier<Component> label, BooleanSupplier ready, Runnable action) {
            this.label = label;
            this.ready = ready;
            this.action = action;
        }

        @Override
        public @NotNull Result onMousePressed(int button) {
            if (button != 0) return Result.IGNORE;
            if (!ready.getAsBoolean()) return Result.SUCCESS;
            pressed = true;
            click();
            action.run();
            return Result.SUCCESS;
        }

        @Override
        public boolean onMouseReleased(int button) {
            pressed = false;
            return true;
        }

        @Override
        public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
            GuiGraphics g = context.getGraphics();
            int w = getArea().width;
            int h = getArea().height;
            boolean ok = ready.getAsBoolean();
            boolean hover = ok && isHovering();
            if (!hover) pressed = false;
            g.fill(0, 0, w, h, 0xFF000000);
            int face = !ok ? 0xFF6B6B6B : (pressed ? 0xFF4A7A4A : hover ? 0xFF6FB36F : 0xFF5C9C5C);
            g.fill(1, 1, w - 1, h - 1, face);
            g.fill(1, 1, w - 1, 2, ok ? 0xFF9BDB9B : 0xFF8A8A8A);
            g.fill(1, h - 2, w - 1, h - 1, ok ? 0xFF2F5F2F : 0xFF4A4A4A);
            String s = fit(label.get().getString(), w - 6, 1f);
            Font font = font();
            g.drawString(font, s, (w - font.width(s)) / 2, (h - 8) / 2 + (pressed ? 1 : 0), ok ? 0xFFFFFFFF : 0xFFC0C0C0, true);
        }
    }
}
