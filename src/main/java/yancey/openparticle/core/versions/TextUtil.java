package yancey.openparticle.core.versions;

import net.minecraft.text.MutableText;

//#if MC>=11900
import net.minecraft.text.Text;
//#else
//$$ import net.minecraft.text.LiteralText;
//#endif

public class TextUtil {

    public static MutableText empty() {
        //#if MC>=11900
        return Text.empty();
        //#else
        //$$ return new LiteralText("");
        //#endif
    }

    public static MutableText literal(String string) {
        //#if MC>=11900
        return Text.literal(string);
        //#else
        //$$ return new LiteralText(string);
        //#endif
    }

}
