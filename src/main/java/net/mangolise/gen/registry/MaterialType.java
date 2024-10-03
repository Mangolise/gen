package net.mangolise.gen.registry;

public enum MaterialType {
    WOOD(0),
    STONE(1),
    PLANTS(2);

    public final int id;

    MaterialType(int id) {
        this.id = id;
    }

    public static MaterialType fromId(int id) {
        return switch (id) {
            case 0 -> WOOD;
            case 1 -> STONE;
            case 2 -> PLANTS;
            default -> throw new IndexOutOfBoundsException("Invalid MaterialType Id");
        };
    }
}
