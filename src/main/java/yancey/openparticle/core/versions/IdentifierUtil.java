package yancey.openparticle.core.versions;

import net.minecraft.util.Identifier;

public class IdentifierUtil {

    public static Identifier create(String namespace, String value) {
        //#if MC>=11900
        return Identifier.of(namespace, value);
        //#else
        //$$ return new Identifier(namespace, value);
        //#endif
    }

}
