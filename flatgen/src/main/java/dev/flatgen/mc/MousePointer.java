package dev.flatgen.mc;

//? if <1.21.11 {
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
//?}

/**
 * The mouse's pointer over Flatgen's screens up to 1.21.1, which has no way of its own to change it: the hand over what
 * can be grabbed, else the arrow. From 1.21.11 the game takes a request for a pointer with each frame itself.
 */
public final class MousePointer {
    //? if <1.21.11 {
    /** The window's hand, made the first time it is shown. */
    private static long hand;
    /** Whether the hand is shown now. */
    private static boolean shown;
    //?}

    private MousePointer() {
    }

    /** Shows the hand, or the arrow again, if the other is shown now. */
    public static void hand(boolean show) {
        //? if <1.21.11 {
        if (show == shown) {
            return;
        }
        if (hand == 0) {
            hand = GLFW.glfwCreateStandardCursor(GLFW.GLFW_HAND_CURSOR);
        }
        GLFW.glfwSetCursor(Minecraft.getInstance().getWindow().getWindow(), show ? hand : 0);
        shown = show;
        //?}
    }
}
