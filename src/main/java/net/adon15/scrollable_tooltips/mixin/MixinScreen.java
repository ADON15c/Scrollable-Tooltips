package net.adon15.scrollable_tooltips.mixin;

import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(Screen.class)
public abstract class MixinScreen
        extends AbstractParentElement {

    private int y_offset = 0;
    private int y_offset_applied = 0;
    private int store_y = 0;
    @Shadow public int height;

    @Shadow public abstract List<Text> getTooltipFromItem(ItemStack stack);

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        y_offset += -10 * amount;
        y_offset = Math.max(y_offset, 0);
        return this.hoveredElement(mouseX, mouseY).filter(element -> element.mouseScrolled(mouseX, mouseY, amount)).isPresent();
    }

    @ModifyArgs(method = "renderTooltip(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/item/ItemStack;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;renderTooltip(Lnet/minecraft/client/util/math/MatrixStack;Ljava/util/List;Ljava/util/Optional;II)V"))
    private void modifyArgs(Args args) {
        args.set(4, (int) args.get(4) + y_offset_applied);
    }

    @Inject(method = "renderTooltip(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/item/ItemStack;II)V", at = @At("HEAD"))
    private void checkNewItem(MatrixStack matrices, ItemStack stack, int x, int y, CallbackInfo ci) {
        List<TooltipComponent> components = this.getTooltipFromItem(stack).stream().map(Text::asOrderedText).map(TooltipComponent::of).collect(Collectors.toList());
        stack.getTooltipData().ifPresent(data -> components.add(1, TooltipComponent.of(data)));

        int y_end = components.size() == 1 ? -2 : 0;
        for (TooltipComponent tooltipComponent : components) {
            y_end += tooltipComponent.getHeight();
        }
        int y_start = y - 12;
        y_end += y_start;


        y_offset_applied = -(y_end - this.height > 0 ? y_end - this.height + 6 : 0);
        if (y_start + y_offset_applied < 6) {
            y_offset_applied += y_offset;
            if (y_start + y_offset_applied >= 6) {
                y_offset_applied = -y_start + 6;
            }
        }
    }

    @Inject(method = "renderTooltipFromComponents", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/client/gui/screen/Screen;width:I"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void storeXY(MatrixStack matrices, List<TooltipComponent> components, int x, int y, CallbackInfo ci, int i, int j, int l, int m, int k, int n) {
        store_y = m;
    }

    @ModifyVariable(method = "renderTooltipFromComponents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V"), index = 8)
    private int loadY(int value) {
        return store_y;
    }
}
