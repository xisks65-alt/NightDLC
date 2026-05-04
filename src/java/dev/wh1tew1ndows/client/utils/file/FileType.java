package dev.wh1tew1ndows.client.utils.file;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FileType {
    CONFIG("config"),
    MACROS("other"),
    FRIEND("other"),
    ACCOUNT("other"),
    STAFF("other"),
    MUSIC("other");
    private final String name;
}
