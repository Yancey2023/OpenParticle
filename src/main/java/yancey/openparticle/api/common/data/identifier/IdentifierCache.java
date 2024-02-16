package yancey.openparticle.api.common.data.identifier;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IdentifierCache {

    public static List<Identifier> identifierList = new ArrayList<>();

    public static void add(Identifier identifier) {
        if (!identifierList.contains(identifier)) {
            identifierList.add(identifier);
        }
    }

    public static int getId(Identifier identifier) {
        for (int i = 0; i < identifierList.size(); i++) {
            if (identifierList.get(i) == identifier) {
                return i;
            }
        }
        return -1;
    }

    public static Identifier getIdentifier(int id) {
        return identifierList.get(id);
    }

    public static void writeToFile(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeInt(identifierList.size());
        for (Identifier identifier : identifierList) {
            identifier.writeToFile(dataOutputStream);
        }
    }

    public static void readFromFile(DataInputStream dataInputStream) throws IOException {
        identifierList.clear();
        int size = dataInputStream.readInt();
        for (int i = 0; i < size; i++) {
            identifierList.add(new Identifier(dataInputStream));
        }
    }
}
