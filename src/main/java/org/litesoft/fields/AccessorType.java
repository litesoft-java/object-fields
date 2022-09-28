package org.litesoft.fields;

public enum AccessorType {
    auto, required, optional() {
        @Override
        public String initialMetaData() {
            return "";
        }
    };

    public String initialMetaData() {
        return name();
    }
}
