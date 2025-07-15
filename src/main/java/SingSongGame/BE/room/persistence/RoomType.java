package SingSongGame.BE.room.persistence;

public enum RoomType {
    KEY_SING_YOU("KEY_SING_YOU"),
    RANDOM_SONG("RANDOM_SONG"),
    PLAIN_SONG("PLAIN_SONG"),
    QUICK_MATCH("QUICK_MATCH");

    private final String roomType;

    RoomType(String roomType) {
        this.roomType = roomType;
    }
}
