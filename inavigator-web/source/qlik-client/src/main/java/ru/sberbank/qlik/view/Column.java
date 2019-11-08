package ru.sberbank.qlik.view;

class Column {
    public Type type;
    public String name;

    public static enum Type {
        UNKNOWN(0),
        TEXT(1),
        REAL(2),
        DATE(3),
        TIME(4),
        TIMESTAMP(5),
        INTERVAL(6),
        INTEGER(10),
        DECIMAL(11),
        MONEY(12);

        private int id;

        Type(int id) {
            this.id = id;
        }

        public static Type resolve(int id) {
            for (Type type : values()) {
                if (type.id == id) return type;
            }
            return UNKNOWN;
        }
    }
}
