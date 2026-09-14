package yancey.openparticle.api.identifier;

import yancey.openparticle.api.run.data.identifier.DataIdentifier;

public class Identifier {

    private final String namespace, value;

    private DataIdentifier dataIdentifier;

    public Identifier(String namespace, String value) {
        this.namespace = namespace;
        this.value = value;
    }

    public Identifier(String value) {
        this("minecraft", value);
    }

    public DataIdentifier getDataIdentifier() {
        if (dataIdentifier == null) {
            dataIdentifier = new DataIdentifier(namespace, value);
        }
        return dataIdentifier;
    }

    public void clearCache() {
        dataIdentifier = null;
    }

    @Override
    public String toString() {
        return namespace + ':' + value;
    }

}
