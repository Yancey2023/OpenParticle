package yancey.openparticle.core.util;

import net.minecraft.util.Identifier;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IdentifierCache {

    public static List<Identifier> identifierList = new ArrayList<>();

    public static Identifier getIdentifier(int id){
        return identifierList.get(id);
    }

    public static void readFromFile(DataInputStream dataInputStream) throws IOException {
        identifierList.clear();
        int size = dataInputStream.readInt();
        for (int i = 0; i < size; i++) {
            if(dataInputStream.readBoolean()){
                identifierList.add(new Identifier(dataInputStream.readUTF()));
            }else{
                identifierList.add(new Identifier(dataInputStream.readUTF(),dataInputStream.readUTF()));
            }
        }
    }
}
